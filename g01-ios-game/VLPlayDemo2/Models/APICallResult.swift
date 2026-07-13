import SwiftUI

enum APICallResult: Equatable {
    case idle
    case loading
    case success(String)
    case failure(String)

    var color: Color {
        switch self {
        case .idle:    return .secondary
        case .loading: return .orange
        case .success: return .green
        case .failure: return .red
        }
    }

    var text: String {
        switch self {
        case .idle:              return "idle"
        case .loading:           return "loading…"
        case .success(let s):    return "✓ \(s)"
        case .failure(let e):    return "✗ \(e)"
        }
    }
}

struct ResultBadge: View {
    let result: APICallResult

    var body: some View {
        Text(result.text)
            .font(.footnote)
            .foregroundColor(result.color)
            .textSelection(.enabled)
    }
}
