param([string]$InputFile, [string]$OutputFile)

$word = New-Object -ComObject Word.Application
$word.Visible = $false
$doc = $word.Documents.Add()
$sel = $word.Selection

$sel.Style = "Normal"
$sel.Font.Name = "Times New Roman"
$sel.Font.Size = 12

$mdContent = Get-Content $InputFile -Encoding UTF8 -Raw
$lines = $mdContent -split "`n"

foreach ($line in $lines) {
    $trimmed = $line.Trim()

    if ($trimmed -eq "") {
        $sel.TypeParagraph()
        continue
    }

    if ($trimmed -eq "---") {
        $sel.TypeParagraph()
        continue
    }

    $isTable = $trimmed.StartsWith("|")
    $isDashLine = ($trimmed -replace "[|\-\s:]", "") -eq ""

    if ($isDashLine) { continue }

    if ($trimmed.StartsWith("#### ")) {
        $sel.Style = "Normal"
        $sel.Font.Name = "Times New Roman"
        $sel.Font.Bold = $true
        $sel.Font.Size = 12
        $sel.TypeText($trimmed.Substring(5))
        $sel.TypeParagraph()
        $sel.Font.Bold = $false
        continue
    }

    if ($trimmed.StartsWith("### ")) {
        $sel.Style = "Normal"
        $sel.Font.Name = "Times New Roman"
        $sel.Font.Bold = $true
        $sel.Font.Size = 13
        $sel.TypeText($trimmed.Substring(4))
        $sel.TypeParagraph()
        $sel.Font.Bold = $false
        continue
    }

    if ($trimmed.StartsWith("## ")) {
        $sel.Style = "Normal"
        $sel.Font.Name = "Times New Roman"
        $sel.Font.Bold = $true
        $sel.Font.Size = 14
        $sel.TypeText($trimmed.Substring(3))
        $sel.TypeParagraph()
        $sel.Font.Bold = $false
        continue
    }

    if ($trimmed.StartsWith("# ")) {
        $sel.Style = "Normal"
        $sel.Font.Name = "Times New Roman"
        $sel.Font.Bold = $true
        $sel.Font.Size = 18
        $sel.TypeText($trimmed.Substring(2))
        $sel.TypeParagraph()
        $sel.Font.Bold = $false
        continue
    }

    if ($isTable) {
        $cellText = $trimmed.Replace("|", "  ")
        $sel.TypeText($cellText.Trim())
        $sel.TypeParagraph()
        continue
    }

    if ($trimmed.StartsWith("- ")) {
        $sel.TypeText("  * " + $trimmed.Substring(2))
        $sel.TypeParagraph()
        continue
    }

    if ($trimmed.StartsWith("> ")) {
        $sel.Font.Italic = $true
        $sel.TypeText($trimmed.Substring(2))
        $sel.TypeParagraph()
        $sel.Font.Italic = $false
        continue
    }

    $sel.TypeText($trimmed)
    $sel.TypeParagraph()
}

$doc.SaveAs([ref]$OutputFile, [ref]16)
$doc.Close()
$word.Quit()
[System.Runtime.InteropServices.Marshal]::ReleaseComObject($word) | Out-Null

Write-Host "Done: $OutputFile"
Write-Host "Size: $((Get-Item $OutputFile).Length) bytes"
