# Chay duoc khi goi tu bat ky thu muc nao.
Set-Location $PSScriptRoot
New-Item -ItemType Directory -Force out | Out-Null
$files = Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object { $_.FullName }
javac -encoding UTF-8 -d out $files
if ($LASTEXITCODE -eq 0) { java -cp out main.Main }
