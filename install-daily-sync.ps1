[CmdletBinding()]
param(
    [ValidatePattern('^(?:[01]\d|2[0-3]):[0-5]\d$')]
    [string]$At = '06:00',
    [ValidateSet('Garmin', 'Strava', 'Both')]
    [string]$Source = 'Garmin',
    [switch]$Uninstall
)

$ErrorActionPreference = 'Stop'
$taskName = 'KikStrava daily sync'

if ($Uninstall) {
    $task = Get-ScheduledTask -TaskName $taskName -ErrorAction SilentlyContinue
    if ($null -ne $task) {
        Unregister-ScheduledTask -TaskName $taskName -Confirm:$false
        Write-Output "Removed scheduled task '$taskName'."
    } else {
        Write-Output "Scheduled task '$taskName' was not registered."
    }
    exit 0
}

$launcher = Join-Path $PSScriptRoot 'sync-daily.bat'
$application = Join-Path $PSScriptRoot 'KikStrava.exe'
$configurationPaths = @(
    (Join-Path $PSScriptRoot 'conf\stravakik.conf'),
    (Join-Path $PSScriptRoot 'app\conf\stravakik.conf')
)
$configuration = $configurationPaths |
    Where-Object { Test-Path -LiteralPath $_ -PathType Leaf } |
    Select-Object -First 1

foreach ($requiredPath in @($launcher, $application)) {
    if (-not (Test-Path -LiteralPath $requiredPath -PathType Leaf)) {
        throw "Required application file not found: $requiredPath"
    }
}
if (-not $configuration) {
    throw 'Configuration file not found in conf\stravakik.conf or app\conf\stravakik.conf'
}

$runAt = [datetime]::Today.Add([timespan]::ParseExact(
    $At, 'hh\:mm', [Globalization.CultureInfo]::InvariantCulture))
$trigger = New-ScheduledTaskTrigger -Daily -At $runAt
$sourceArgument = '--source={0}' -f $Source.ToLowerInvariant()
$action = New-ScheduledTaskAction `
    -Execute $env:ComSpec `
    -Argument ('/d /c ""{0}" {1}"' -f $launcher, $sourceArgument) `
    -WorkingDirectory $PSScriptRoot
$principal = New-ScheduledTaskPrincipal `
    -UserId ([Security.Principal.WindowsIdentity]::GetCurrent().Name) `
    -LogonType Interactive `
    -RunLevel Limited
$settings = New-ScheduledTaskSettingsSet `
    -StartWhenAvailable `
    -MultipleInstances IgnoreNew `
    -ExecutionTimeLimit (New-TimeSpan -Hours 1)

Register-ScheduledTask `
    -TaskName $taskName `
    -Action $action `
    -Trigger $trigger `
    -Principal $principal `
    -Settings $settings `
    -Description 'Synchronizes recent Strava and Garmin activities to Kikourou.' `
    -Force | Out-Null

Write-Output "Registered '$taskName' to run daily at $At using $Source for the current Windows user."
Write-Output "The task runs when that user is signed in. Log: $env:LOCALAPPDATA\KikStrava\sync.log"
