import SwiftUI
import VLPlaySDK

// IAA "Ads Test" console (P3 parity with the Android AdsTestScreen). Exercises
// the provider-agnostic VLPlayAds facade: status, per-placement preload/show,
// and a live event log.
//
// INTEGRITY DEMO: the "🎁 REWARD GRANTED" line fires ONLY on
// `vlplayAdRewardConfirmed:` (BE SSV verified) — never on the advisory
// `vlplayAdUserRewarded:`. This is the pattern partner games must copy.
//
// NOTE: real fills need the AppLovin pod in the app + the CMS ads config
// (sdkKey + unit ids) enabled for this game. Without them the facade returns
// provider-neutral errors (ads disabled / not ready) — logged below.

final class AdsTestModel: NSObject, ObservableObject {
    @Published var enabled = false
    @Published var initialized = false
    @Published var lastReward: String = ""
    @Published var log: [String] = []

    private var timer: Timer?

    func startPolling() {
        refresh()
        timer = Timer.scheduledTimer(withTimeInterval: 1.0, repeats: true) { [weak self] _ in
            self?.refresh()
        }
    }

    func stopPolling() {
        timer?.invalidate()
        timer = nil
    }

    private func refresh() {
        enabled = VLPlayAds.isEnabled()
        initialized = VLPlayAds.isInitialized()
    }

    func append(_ line: String) {
        let ts = DateFormatter.localizedString(from: Date(), dateStyle: .none, timeStyle: .medium)
        log.insert("[\(ts)] \(line)", at: 0)
        if log.count > 60 { log.removeLast(log.count - 60) }
    }

    func preload(_ placement: String) {
        append("preload(\(placement))")
        VLPlayAds.preload(placement, delegate: self)
    }

    func isReady(_ placement: String) {
        append("isReady(\(placement)) = \(VLPlayAds.isReady(placement))")
    }

    func showRewarded(_ placement: String) {
        append("showRewarded(\(placement))")
        VLPlayAds.showRewarded(placement, from: nil, delegate: self)
    }

    func showInterstitial(_ placement: String) {
        append("showInterstitial(\(placement))")
        VLPlayAds.showInterstitial(placement, from: nil, delegate: self)
    }
}

extension AdsTestModel: VLPlayAdLoadDelegate, VLPlayRewardedAdDelegate, VLPlayInterstitialAdDelegate {
    func vlplayAdLoaded(_ placementId: String) { append("✅ loaded \(placementId)") }
    func vlplayAdLoadFailed(_ placementId: String, error: VLPlayAdError) {
        append("⚠️ loadFailed \(placementId): \(error.code) — \(error.message)")
    }
    func vlplayAdShown(_ placementId: String) { append("▶️ shown \(placementId)") }
    func vlplayAdClicked(_ placementId: String) { append("👆 clicked \(placementId)") }
    func vlplayAdDismissed(_ placementId: String) { append("⏹ dismissed \(placementId)") }
    func vlplayAdShowFailed(_ placementId: String, error: VLPlayAdError) {
        append("⚠️ showFailed \(placementId): \(error.code) — \(error.message)")
    }
    // ADVISORY — never grant here.
    func vlplayAdUserRewarded(_ placementId: String, reward: VLPlayAdReward) {
        append("… advisory reward \(placementId) (waiting for SSV)")
    }
    // The ONLY grant path (BE SSV verified).
    func vlplayAdRewardConfirmed(_ placementId: String, reward serverReward: VLPlayAdReward) {
        let msg = "\(serverReward.amount) \(serverReward.currency)"
        lastReward = msg
        append("🎁 REWARD GRANTED \(placementId): \(msg)")
    }
}

struct AdsTestView: View {
    @StateObject private var model = AdsTestModel()
    @State private var placement: String = "rewarded_test"

    var body: some View {
        VStack(spacing: 0) {
            VLTopAppBar(variant: .debugger, environmentBadge: "STAGING")
            ScrollView {
                VStack(alignment: .leading, spacing: VLSpacing.md) {
                    statusCard
                    controls
                    if !model.lastReward.isEmpty {
                        Text("Last granted reward: \(model.lastReward)")
                            .font(VLFont.bodyLg.weight(.semibold))
                            .foregroundColor(VLColor.primary)
                    }
                    logCard
                }
                .padding(VLSpacing.md)
            }
            .background(VLColor.surface)
        }
        .background(VLColor.surface.ignoresSafeArea())
        .navigationTitle("Ads Test")
        .navigationBarTitleDisplayMode(.inline)
        .onAppear { model.startPolling() }
        .onDisappear { model.stopPolling() }
    }

    private var statusCard: some View {
        VStack(alignment: .leading, spacing: VLSpacing.sm) {
            Text("Status").font(VLFont.bodyLg.weight(.bold)).foregroundColor(VLColor.onSurface)
            statusRow("Enabled (CMS ads block)", model.enabled)
            statusRow("Provider initialized", model.initialized)
            if !model.enabled {
                Text("Ads are disabled for this game. Enable them in the CMS ads config and add the AppLovin pod to the app to get real fills.")
                    .font(VLFont.bodySm)
                    .foregroundColor(VLColor.onSurfaceVariant)
            }
        }
        .padding(VLSpacing.md)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(VLColor.surfaceContainerLow)
        .cornerRadius(12)
    }

    private func statusRow(_ label: String, _ value: Bool) -> some View {
        HStack {
            Image(systemName: value ? VLIcon.success : VLIcon.errorOctagon)
                .foregroundColor(value ? VLColor.primary : VLColor.onSurfaceVariant)
            Text(label).font(VLFont.bodyMd).foregroundColor(VLColor.onSurface)
            Spacer()
            Text(value ? "YES" : "NO").font(VLFont.bodyMd.weight(.semibold))
                .foregroundColor(value ? VLColor.primary : VLColor.onSurfaceVariant)
        }
    }

    private var controls: some View {
        VStack(alignment: .leading, spacing: VLSpacing.sm) {
            Text("Placement").font(VLFont.bodyLg.weight(.bold)).foregroundColor(VLColor.onSurface)
            TextField("placement id", text: $placement)
                .textFieldStyle(.roundedBorder)
                .autocorrectionDisabled()
                .textInputAutocapitalization(.never)
            HStack(spacing: VLSpacing.sm) {
                button("Preload") { model.preload(placement) }
                button("isReady") { model.isReady(placement) }
            }
            HStack(spacing: VLSpacing.sm) {
                button("Show Rewarded") { model.showRewarded(placement) }
                button("Show Interstitial") { model.showInterstitial(placement) }
            }
        }
    }

    private func button(_ title: String, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Text(title)
                .font(VLFont.bodyMd.weight(.semibold))
                .frame(maxWidth: .infinity)
                .padding(.vertical, 10)
                .background(VLColor.primary.opacity(placement.isEmpty ? 0.4 : 1))
                .foregroundColor(.white)
                .cornerRadius(10)
        }
        .disabled(placement.isEmpty)
    }

    private var logCard: some View {
        VStack(alignment: .leading, spacing: VLSpacing.sm) {
            HStack {
                Text("Event log").font(VLFont.bodyLg.weight(.bold)).foregroundColor(VLColor.onSurface)
                Spacer()
                Button("Clear") { model.log.removeAll() }
                    .font(VLFont.bodySm).foregroundColor(VLColor.primary)
            }
            if model.log.isEmpty {
                Text("No events yet.").font(VLFont.bodySm).foregroundColor(VLColor.onSurfaceVariant)
            } else {
                ForEach(Array(model.log.enumerated()), id: \.offset) { _, line in
                    Text(line)
                        .font(.system(size: 12, design: .monospaced))
                        .foregroundColor(VLColor.onSurface)
                        .frame(maxWidth: .infinity, alignment: .leading)
                }
            }
        }
        .padding(VLSpacing.md)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(VLColor.surfaceContainerLow)
        .cornerRadius(12)
    }
}
