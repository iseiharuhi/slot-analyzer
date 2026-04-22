使い方
1. この zip を解凍
2. delete_bak_files.ps1 をプロジェクトルートに置くか、そのまま実行
3. 例:
   powershell -ExecutionPolicy Bypass -File .\delete_bak_files.ps1 -ProjectRoot "C:\Users\iseih\OneDrive\Desktop\SlotSettingAnalyzerCompleteV2"

対象
- app/src/main/assets/machines 配下の *.json.bak を一括削除

注意
- manifest は触りません
- .json 本体は削除しません
