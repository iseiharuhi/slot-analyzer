SlotSettingAnalyzer Feature Refactor v3

Changes in v3:
- Unified UI-layer placement under presentation/ for each feature
- Kept feature-specific domain models/usecases under each feature/domain
- Retained shared layers in core/data/di/domain for cross-feature concerns
- Navigation imports updated to new presentation packages

Suggested feature structure:
- feature/home/presentation
- feature/machine/presentation + domain
- feature/session/presentation + domain
- feature/inference/presentation + domain
- feature/history/list/presentation
- feature/history/detail/presentation
- feature/history/domain
- core/navigation, core/ui, data, di, shared domain

Notes:
- This refactor focused on package/file organization.
- Android Studio Sync / Rebuild is still recommended after replacing the project.
