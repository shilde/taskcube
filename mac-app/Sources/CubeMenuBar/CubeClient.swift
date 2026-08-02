import Foundation

/// WebSocket client for the cube's protocol (see ../cube/README.md).
/// Mirrors receiver/wuerfel_receiver.py's connect/reconnect behavior.
final class CubeClient {
    private let url: URL
    private let session = URLSession(configuration: .default)
    private var task: URLSessionWebSocketTask?

    var onFaceChange: ((Int, String) -> Void)?
    var onStatusChange: ((String) -> Void)?

    init(url: URL) {
        self.url = url
    }

    func connect() {
        onStatusChange?("Connecting…")
        let task = session.webSocketTask(with: url)
        self.task = task
        task.resume()
        receive()
    }

    private func receive() {
        task?.receive { [weak self] result in
            guard let self else { return }
            switch result {
            case .failure:
                self.scheduleReconnect()
            case .success(let message):
                switch message {
                case .string(let text):
                    self.handle(text)
                case .data(let data):
                    if let text = String(data: data, encoding: .utf8) {
                        self.handle(text)
                    }
                @unknown default:
                    break
                }
                self.receive()
            }
        }
    }

    private func handle(_ text: String) {
        guard let data = text.data(using: .utf8),
              let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
              let type = json["type"] as? String else { return }

        switch type {
        case "hello":
            let face = json["face"] as? Int ?? 0
            // Initial state on connect, not a user-triggered change: update the
            // status item but don't fire a notification for it.
            onStatusChange?(face > 0 ? FaceNames.name(for: face) : "Connected")
        case "face_change":
            let face = json["face"] as? Int ?? 0
            onFaceChange?(face, FaceNames.name(for: face))
        default:
            break
        }
    }

    private func scheduleReconnect() {
        task = nil
        onStatusChange?("Disconnected — retrying…")
        DispatchQueue.main.asyncAfter(deadline: .now() + 5) { [weak self] in
            self?.connect()
        }
    }
}
