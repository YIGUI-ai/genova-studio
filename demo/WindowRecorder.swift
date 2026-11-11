import AVFoundation
import Cocoa
import CoreGraphics
import CoreVideo
import Foundation

final class Recorder {
    let outputURL: URL
    let windowTitle: String
    let fps: Int32
    let maxSeconds: Double
    var writer: AVAssetWriter?
    var input: AVAssetWriterInput?
    var adaptor: AVAssetWriterInputPixelBufferAdaptor?
    var started = false
    var startTime = CMTime.zero
    var stopRequested = false
    var frameCount = 0

    init(outputURL: URL, windowTitle: String, fps: Int32 = 30, maxSeconds: Double = 600) {
        self.outputURL = outputURL; self.windowTitle = windowTitle; self.fps = fps; self.maxSeconds = maxSeconds
    }

    func windowID() -> CGWindowID? {
        guard let list = CGWindowListCopyWindowInfo([.optionOnScreenOnly, .excludeDesktopElements], kCGNullWindowID) as? [[String: Any]] else { return nil }
        var bestMain: (area: Double, id: CGWindowID)?
        var importWindow: CGWindowID?
        for item in list {
            guard let layer = item[kCGWindowLayer as String] as? Int, layer == 0,
                  let owner = item[kCGWindowOwnerName as String] as? String,
                  let title = item[kCGWindowName as String] as? String,
                  owner == "GENOVA Studio",
                  (title == windowTitle || title == "Import long-format data"),
                  let number = item[kCGWindowNumber as String] as? UInt32,
                  let bounds = item[kCGWindowBounds as String] as? NSDictionary,
                  let width = (bounds["Width"] as? NSNumber)?.doubleValue,
                  let height = (bounds["Height"] as? NSNumber)?.doubleValue else { continue }
            let candidate = (area: width * height, id: CGWindowID(number))
            if title == "Import long-format data" {
                // CGWindowListCopyWindowInfo is front-to-back. Prefer the
                // visible import sheet while it is open so the recording
                // shows the privacy-safe file selection step.
                if importWindow == nil { importWindow = candidate.id }
            } else if bestMain == nil || candidate.area > bestMain!.area {
                bestMain = candidate
            }
        }
        return importWindow ?? bestMain?.id
    }

    func run() throws {
        let deadline = Date().addingTimeInterval(maxSeconds)
        let noWindowDeadline = Date().addingTimeInterval(15)
        var next = Date()
        while Date() < deadline && !stopRequested {
            if let id = windowID(), let image = CGWindowListCreateImage(.null, .optionIncludingWindow, id, [.bestResolution, .boundsIgnoreFraming]) {
                try append(image: image)
            } else if Date() > noWindowDeadline && frameCount == 0 {
                throw NSError(domain: "GENOVAWindowRecorder", code: 1, userInfo: [NSLocalizedDescriptionKey: "No matching GENOVA window frames were captured. Check Screen Recording permission and the window title."])
            }
            next = next.addingTimeInterval(1.0 / Double(fps))
            let wait = next.timeIntervalSinceNow
            if wait > 0 { Thread.sleep(forTimeInterval: wait) }
        }
        finish()
    }

    func append(image: CGImage) throws {
        let width = image.width & ~1, height = image.height & ~1
        guard width > 0, height > 0 else { return }
        if !started {
            try start(width: width, height: height)
            startTime = CMTime.zero; started = true
        }
        guard let input, input.isReadyForMoreMediaData, let adaptor else { return }
        var buffer: CVPixelBuffer?
        CVPixelBufferCreate(kCFAllocatorDefault, width, height, kCVPixelFormatType_32BGRA,
                            [kCVPixelBufferCGImageCompatibilityKey: true, kCVPixelBufferCGBitmapContextCompatibilityKey: true] as CFDictionary, &buffer)
        guard let buffer else { return }
        CVPixelBufferLockBaseAddress(buffer, [])
        defer { CVPixelBufferUnlockBaseAddress(buffer, []) }
        guard let base = CVPixelBufferGetBaseAddress(buffer) else { return }
        let context = CGContext(data: base, width: width, height: height,
                                bitsPerComponent: 8, bytesPerRow: CVPixelBufferGetBytesPerRow(buffer),
                                space: CGColorSpaceCreateDeviceRGB(), bitmapInfo: CGImageAlphaInfo.premultipliedFirst.rawValue | CGBitmapInfo.byteOrder32Little.rawValue)
        context?.draw(image, in: CGRect(x: 0, y: 0, width: width, height: height))
        let elapsed = CMTime(seconds: Date().timeIntervalSinceReferenceDate - startClock, preferredTimescale: 600)
        if adaptor.append(buffer, withPresentationTime: elapsed) { frameCount += 1 }
    }

    var startClock = Date().timeIntervalSinceReferenceDate

    func start(width: Int, height: Int) throws {
        try? FileManager.default.removeItem(at: outputURL)
        writer = try AVAssetWriter(outputURL: outputURL, fileType: .mp4)
        let settings: [String: Any] = [AVVideoCodecKey: AVVideoCodecType.h264,
            AVVideoWidthKey: width, AVVideoHeightKey: height,
            AVVideoCompressionPropertiesKey: [AVVideoAverageBitRateKey: 8_000_000,
                AVVideoExpectedSourceFrameRateKey: fps, AVVideoMaxKeyFrameIntervalKey: Int(fps)*2]]
        input = AVAssetWriterInput(mediaType: .video, outputSettings: settings)
        input!.expectsMediaDataInRealTime = true
        adaptor = AVAssetWriterInputPixelBufferAdaptor(assetWriterInput: input!, sourcePixelBufferAttributes: [kCVPixelBufferPixelFormatTypeKey as String: kCVPixelFormatType_32BGRA])
        writer!.add(input!); writer!.startWriting(); writer!.startSession(atSourceTime: .zero)
        startClock = Date().timeIntervalSinceReferenceDate
    }

    func finish() {
        input?.markAsFinished()
        guard let writer else { return }
        let semaphore = DispatchSemaphore(value: 0)
        writer.finishWriting {
            print("Wrote \(self.outputURL.path) (\(self.frameCount) frames)")
            semaphore.signal()
        }
        _ = semaphore.wait(timeout: .now() + 10)
    }
}

let args = CommandLine.arguments
guard args.count >= 3 else { fputs("usage: genova-window-recorder <window-title> <output.mp4> [seconds]\n", stderr); exit(2) }
let seconds = args.count > 3 ? Double(args[3]) ?? 600 : 600
let recorder = Recorder(outputURL: URL(fileURLWithPath: args[2]), windowTitle: args[1], maxSeconds: seconds)
signal(SIGINT, SIG_IGN)
let interrupt = DispatchSource.makeSignalSource(signal: SIGINT, queue: .main)
interrupt.setEventHandler { recorder.stopRequested = true }
interrupt.resume()
do { try recorder.run() } catch { fputs("Recorder error: \(error)\n", stderr); exit(1) }
