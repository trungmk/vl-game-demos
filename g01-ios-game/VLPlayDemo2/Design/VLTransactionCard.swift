import SwiftUI

// Purchase History row (d3). Title + amount + datetime + status pill + debug box.

enum VLTransactionStatus { case success, pending, failed

    var pillText: String {
        switch self {
        case .success: return "SUCCESS"
        case .pending: return "PENDING"
        case .failed:  return "FAILED"
        }
    }
    var pillTone: VLPillTone {
        switch self {
        case .success: return .success
        case .pending: return .warning
        case .failed:  return .error
        }
    }
}

struct VLTransactionCardData: Identifiable {
    let id = UUID()
    let title: String
    let amount: String           // "$5.00" formatted
    let datetime: String         // "2026-04-27 14:32:18"
    let status: VLTransactionStatus
    let purchaseCode: String?
    let errorReason: String?     // only when status == .failed
}

struct VLTransactionCard: View {
    let data: VLTransactionCardData

    var body: some View {
        VStack(alignment: .leading, spacing: VLSpacing.sm) {
            HStack(alignment: .top) {
                VStack(alignment: .leading, spacing: 2) {
                    Text(data.title)
                        .font(VLFont.bodyLg.weight(.semibold))
                        .foregroundColor(VLColor.onSurface)
                    Text(data.datetime)
                        .font(VLFont.bodySm)
                        .foregroundColor(VLColor.onSurfaceVariant)
                }
                Spacer()
                VStack(alignment: .trailing, spacing: VLSpacing.xs) {
                    Text(data.amount)
                        .font(VLFont.h2)
                        .foregroundColor(VLColor.onSurface)
                    VLStatePill(text: data.status.pillText, tone: data.status.pillTone)
                }
            }

            if let code = data.purchaseCode {
                let boxLines: [String] = {
                    if let err = data.errorReason {
                        return ["purchaseCode: \(code)", "error: \(err)"]
                    }
                    return ["purchaseCode: \(code)"]
                }()
                VLDebugBox(title: nil,
                           lines: boxLines,
                           tone: data.status == .failed ? .error : .info)
            }
        }
        .padding(VLSpacing.md)
        .background(VLColor.surfaceContainerLowest)
        .clipShape(RoundedRectangle(cornerRadius: VLRadius.md, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: VLRadius.md, style: .continuous)
                .stroke(VLColor.outlineVariant, lineWidth: 1)
        )
    }
}
