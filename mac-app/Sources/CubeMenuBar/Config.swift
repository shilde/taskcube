import Cocoa

enum Config {
    // IP read from the ESP32's Serial Monitor, same as receiver/wuerfel_receiver.py's WUERFEL_URL.
    static let cubeURL = URL(string: "ws://192.168.178.113:81")!
}

enum FaceNames {
    static let names: [Int: String] = [
        1: "Face 1 (Z up)",
        2: "Face 2 (Z down)",
        3: "Face 3 (X up)",
        4: "Face 4 (X down)",
        5: "Face 5 (Y up)",
        6: "Face 6 (Y down)",
    ]

    static func name(for face: Int) -> String {
        names[face] ?? "Face \(face)"
    }
}

enum FaceStyle {
    // One accent color per face so the HUD reads at a glance, not just by text.
    private static let colors: [Int: NSColor] = [
        1: .systemBlue,
        2: .systemGreen,
        3: .systemOrange,
        4: .systemPink,
        5: .systemPurple,
        6: .systemTeal,
    ]

    static func color(for face: Int) -> NSColor {
        colors[face] ?? .controlAccentColor
    }

    // SF Symbols' die-face glyphs (macOS 13+) match the cube's 1-6 faces one-to-one.
    static func symbolName(for face: Int) -> String {
        (1...6).contains(face) ? "die.face.\(face).fill" : "questionmark.square.fill"
    }
}
