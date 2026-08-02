import Cocoa

/// Shows a small HUD in the top-right corner on a face change: a colored
/// die-face icon with a spring "pop" entrance, held for ~1.6s, then fades
/// out. Runs alongside the system notification, not instead of it.
final class HUDController {
    private let panel = HUDPanel()
    private let iconView = NSImageView()
    private let label = NSTextField(labelWithString: "")
    private var dismissWorkItem: DispatchWorkItem?

    init() {
        guard let contentView = panel.contentView else { return }

        let visualEffect = NSVisualEffectView(frame: contentView.bounds)
        visualEffect.autoresizingMask = [.width, .height]
        visualEffect.material = .hudWindow
        visualEffect.state = .active
        visualEffect.wantsLayer = true
        visualEffect.layer?.cornerRadius = 20
        visualEffect.layer?.masksToBounds = true
        visualEffect.layer?.borderWidth = 1
        visualEffect.layer?.borderColor = NSColor.white.withAlphaComponent(0.12).cgColor

        iconView.frame = NSRect(x: 0, y: 56, width: contentView.bounds.width, height: 70)
        iconView.autoresizingMask = [.width]
        iconView.imageScaling = .scaleProportionallyUpOrDown
        iconView.wantsLayer = true
        iconView.layer?.shadowOffset = NSSize(width: 0, height: -1)
        iconView.layer?.shadowRadius = 10
        iconView.layer?.shadowOpacity = 0.6

        label.frame = NSRect(x: 8, y: 14, width: contentView.bounds.width - 16, height: 26)
        label.autoresizingMask = [.width]
        label.font = .systemFont(ofSize: 14, weight: .semibold)
        label.textColor = .labelColor
        label.alignment = .center
        label.lineBreakMode = .byTruncatingTail

        visualEffect.addSubview(iconView)
        visualEffect.addSubview(label)
        panel.contentView = visualEffect
    }

    func show(face: Int, name: String) {
        dismissWorkItem?.cancel()

        let color = FaceStyle.color(for: face)
        label.stringValue = name

        let config = NSImage.SymbolConfiguration(pointSize: 40, weight: .regular)
            .applying(.init(paletteColors: [.white, color]))
        iconView.image = NSImage(systemSymbolName: FaceStyle.symbolName(for: face), accessibilityDescription: name)?
            .withSymbolConfiguration(config)
        iconView.layer?.shadowColor = color.cgColor

        guard let screen = NSScreen.main else { return }
        let margin: CGFloat = 20
        let finalOrigin = NSPoint(
            x: screen.visibleFrame.maxX - panel.frame.width - margin,
            y: screen.visibleFrame.maxY - panel.frame.height - margin
        )

        panel.setFrameOrigin(finalOrigin)
        panel.alphaValue = 0
        panel.orderFrontRegardless()

        // Spring "pop": scale in from slightly small/faded to full size.
        if let layer = panel.contentView?.layer {
            layer.transform = CATransform3DIdentity
            let pop = CASpringAnimation(keyPath: "transform.scale")
            pop.fromValue = 0.82
            pop.toValue = 1.0
            pop.damping = 11
            pop.stiffness = 220
            pop.mass = 1
            pop.duration = pop.settlingDuration
            layer.add(pop, forKey: "pop")
        }
        NSAnimationContext.runAnimationGroup { ctx in
            ctx.duration = 0.18
            ctx.timingFunction = CAMediaTimingFunction(name: .easeOut)
            panel.animator().alphaValue = 1
        }

        let workItem = DispatchWorkItem { [weak self] in
            guard let self else { return }
            NSAnimationContext.runAnimationGroup({ ctx in
                ctx.duration = 0.35
                ctx.timingFunction = CAMediaTimingFunction(name: .easeIn)
                self.panel.animator().alphaValue = 0
            }, completionHandler: {
                self.panel.orderOut(nil)
            })
        }
        dismissWorkItem = workItem
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.6, execute: workItem)
    }
}
