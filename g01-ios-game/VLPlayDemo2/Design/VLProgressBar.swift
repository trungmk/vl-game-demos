import SwiftUI

// Linear progress bar — used for Playtime in g1 Anti-Addiction.
// 0...1 progress, fills with primary color, container outlineVariant.

struct VLProgressBar: View {
    let progress: Double // 0.0 ... 1.0
    var height: CGFloat = 8
    var fillColor: Color = VLColor.primary

    var body: some View {
        GeometryReader { geo in
            ZStack(alignment: .leading) {
                Capsule()
                    .fill(VLColor.outlineVariant)
                Capsule()
                    .fill(fillColor)
                    .frame(width: max(0, min(1, progress)) * geo.size.width)
            }
        }
        .frame(height: height)
    }
}
