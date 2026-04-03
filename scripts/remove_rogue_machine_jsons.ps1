$files = @(
  "app/src/main/assets/machines/karakuri.json",
  "app/src/main/assets/machines/lupin.json",
  "app/src/main/assets/machines/tokyoghoul.json",
  "app/src/main/assets/machines/vvv.json"
)

foreach ($file in $files) {
  if (Test-Path $file) {
    Remove-Item $file -Force
    Write-Host "Removed $file"
  } else {
    Write-Host "Skip (not found): $file"
  }
}
