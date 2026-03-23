# Git反映ガイド

## 1. 作業ブランチ作成
git checkout -b docs/v0.4-design-refresh

## 2. ドキュメント配置
docs/SlotSettingAnalyzer_詳細設計書_v0_4.docx
docs/SlotSettingAnalyzer_詳細設計書_v0_4.pdf
docs/CHANGELOG_v0.4.md
docs/GIT_PUSH_GUIDE.md

## 3. 追加
git add docs/

## 4. コミット
git commit -m "docs: refresh design spec and revision list"

## 5. Push
git push -u origin docs/v0.4-design-refresh

## 6. mainへ載せる場合
git checkout main
git merge docs/v0.4-design-refresh
git push origin main

## 補足
- 既存のソース修正も同時に載せる場合は git add app/ docs/ でまとめてコミットしてよい
- 設計書だけ先に上げるなら docs/ 単体コミットの方が履歴が見やすい
