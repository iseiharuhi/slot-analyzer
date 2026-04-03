$files = @(
    "app/src/main/assets/machines/list_slot_001.json",
"app/src/main/assets/machines/list_slot_002.json",
"app/src/main/assets/machines/list_slot_005.json",
"app/src/main/assets/machines/list_slot_006.json",
"app/src/main/assets/machines/list_slot_007.json",
"app/src/main/assets/machines/list_slot_008.json",
"app/src/main/assets/machines/list_slot_011.json",
"app/src/main/assets/machines/list_slot_014.json",
"app/src/main/assets/machines/list_slot_015.json",
"app/src/main/assets/machines/list_slot_016.json",
"app/src/main/assets/machines/list_slot_017.json",
"app/src/main/assets/machines/list_slot_018.json",
"app/src/main/assets/machines/list_slot_019.json",
"app/src/main/assets/machines/list_slot_020.json",
"app/src/main/assets/machines/list_slot_021.json",
"app/src/main/assets/machines/list_slot_022.json",
"app/src/main/assets/machines/list_slot_023.json",
"app/src/main/assets/machines/list_slot_024.json",
"app/src/main/assets/machines/list_slot_025.json",
"app/src/main/assets/machines/dmm_slot_019.json",
"app/src/main/assets/machines/enen.json"
  )

  foreach ($file in $files) {
    if (Test-Path $file) {
      Remove-Item $file -Force
      Write-Host "Removed $file"
    } else {
      Write-Host "Skip (not found): $file"
    }
  }
