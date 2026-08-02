import Cocoa
import UserNotifications

final class AppDelegate: NSObject, NSApplicationDelegate {
    private var statusItem: NSStatusItem!
    private var currentFaceItem: NSMenuItem!
    private var client: CubeClient!
    private let hud = HUDController()

    func applicationDidFinishLaunching(_ notification: Notification) {
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound]) { _, _ in }

        statusItem = NSStatusBar.system.statusItem(withLength: NSStatusItem.variableLength)
        statusItem.button?.title = "🎲"

        let menu = NSMenu()
        currentFaceItem = NSMenuItem(title: "Connecting…", action: nil, keyEquivalent: "")
        currentFaceItem.isEnabled = false
        menu.addItem(currentFaceItem)
        menu.addItem(.separator())
        menu.addItem(NSMenuItem(title: "Quit", action: #selector(quit), keyEquivalent: "q"))
        statusItem.menu = menu

        client = CubeClient(url: Config.cubeURL)
        client.onStatusChange = { [weak self] text in
            DispatchQueue.main.async { self?.updateMenu(text: text) }
        }
        client.onFaceChange = { [weak self] face, name in
            DispatchQueue.main.async {
                self?.updateMenu(text: name)
                self?.notify(faceName: name)
                self?.hud.show(face: face, name: name)
            }
        }
        client.connect()
    }

    private func updateMenu(text: String) {
        currentFaceItem.title = text
        statusItem.button?.toolTip = text
    }

    private func notify(faceName: String) {
        let content = UNMutableNotificationContent()
        content.title = "Cube"
        content.body = "Switched to \(faceName)"
        let request = UNNotificationRequest(identifier: UUID().uuidString, content: content, trigger: nil)
        UNUserNotificationCenter.current().add(request)
    }

    @objc private func quit() {
        NSApplication.shared.terminate(nil)
    }
}
