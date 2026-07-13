import SwiftUI

struct VLCard<Content: View>: View {
    var title: String?
    var footnote: String?
    @ViewBuilder var content: () -> Content

    var body: some View {
        VStack(alignment: .leading, spacing: VLMetric.spacing) {
            if let title {
                Text(title).font(VLFont.sectionTitle)
            }
            content()
            if let footnote {
                Text(footnote)
                    .font(VLFont.caption)
                    .foregroundColor(VLColor.mutedText)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(VLMetric.cardPadding)
        .background(VLColor.cardBackground)
        .clipShape(RoundedRectangle(cornerRadius: VLMetric.cardCorner, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: VLMetric.cardCorner, style: .continuous)
                .stroke(VLColor.border, lineWidth: 0.5)
        )
    }
}

struct VLSectionHeader: View {
    let title: String
    var subtitle: String?
    var body: some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(title).font(VLFont.sectionTitle)
            if let subtitle { Text(subtitle).font(VLFont.caption).foregroundColor(VLColor.mutedText) }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(.horizontal, 4)
    }
}

struct VLRowButton: View {
    let title: String
    var subtitle: String? = nil
    var icon: String = "chevron.right"
    var tint: Color = VLColor.primary
    var destructive: Bool = false
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack(spacing: 12) {
                VStack(alignment: .leading, spacing: 2) {
                    Text(title)
                        .font(VLFont.rowTitle)
                        .foregroundColor(destructive ? VLColor.danger : VLColor.onSurface)
                    if let subtitle {
                        Text(subtitle).font(VLFont.caption).foregroundColor(VLColor.mutedText)
                    }
                }
                Spacer()
                Image(systemName: icon)
                    .font(.system(size: 13, weight: .semibold))
                    .foregroundColor(destructive ? VLColor.danger.opacity(0.6) : tint.opacity(0.75))
            }
            .padding(.vertical, 10)
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
    }
}

struct VLFieldRow: View {
    let label: String
    @Binding var value: String
    var placeholder: String = ""
    var keyboard: UIKeyboardType = .default

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(label).font(VLFont.caption).foregroundColor(VLColor.mutedText)
            TextField(placeholder, text: $value)
                .keyboardType(keyboard)
                .foregroundColor(VLColor.onSurface)
                .autocapitalization(.none)
                .disableAutocorrection(true)
                .padding(.vertical, 10)
                .padding(.horizontal, 14)
                .background(VLColor.surface)
                .clipShape(RoundedRectangle(cornerRadius: 10, style: .continuous))
                .overlay(
                    RoundedRectangle(cornerRadius: 10, style: .continuous)
                        .stroke(VLColor.border, lineWidth: 0.75)
                )
        }
    }
}
