// swift-tools-version:5.9
import PackageDescription

let package = Package(
    name: "CubeMenuBar",
    platforms: [.macOS(.v13)],
    targets: [
        .executableTarget(
            name: "CubeMenuBar",
            path: "Sources/CubeMenuBar"
        )
    ]
)
