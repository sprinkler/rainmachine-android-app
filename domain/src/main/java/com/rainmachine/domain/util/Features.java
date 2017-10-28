package com.rainmachine.domain.util;

import com.rainmachine.domain.boundary.data.SprinklerPrefRepository;

public class Features {

    public static boolean DEV_SPRINKLER_SPK1_OLD_API = false; // used only for testing through
    // dev drawer

    private SprinklerPrefRepository sprinklerPrefRepository;

    public Features(SprinklerPrefRepository sprinklerPrefRepository) {
        this.sprinklerPrefRepository = sprinklerPrefRepository;
    }

    /* Features for device functionality */

    public boolean isSpk1() {
        return is1x(sprinklerPrefRepository.hardwareVersion());
    }

    public boolean isSpk2() {
        return is2x(sprinklerPrefRepository.hardwareVersion()) || isSimulator
                (sprinklerPrefRepository.hardwareVersion());
    }

    public boolean isSpk3() {
        return is3x(sprinklerPrefRepository.hardwareVersion());
    }

    public boolean isAtLeastSpk2() {
        return isAtLeast2x(sprinklerPrefRepository.hardwareVersion()) || isSimulator
                (sprinklerPrefRepository.hardwareVersion());
    }

    public boolean useNewApi() {
        return isAtLeast364(sprinklerPrefRepository.apiVersion()) && (isAtLeastSpk2() ||
                !DEV_SPRINKLER_SPK1_OLD_API);
    }

    public boolean isApiAtLeast41() {
        return isAtLeast41(sprinklerPrefRepository.apiVersion());
    }

    public boolean isApiAtLeast42() {
        return isAtLeast42(sprinklerPrefRepository.apiVersion());
    }

    public boolean isApiAtLeast43() {
        return isAtLeast43(sprinklerPrefRepository.apiVersion());
    }

    public boolean isApiAtLeast44() {
        return isAtLeast44(sprinklerPrefRepository.apiVersion());
    }

    public boolean canBeEmptyPassword() {
        return isAtLeast4x(sprinklerPrefRepository.apiVersion());
    }

    public boolean hasRemoteAccess() {
        return useNewApi() && isAtLeast364(sprinklerPrefRepository.apiVersion());
    }

    public boolean hasBackup() {
        return true;
    }

    public boolean hasStartTimeParams() {
        return isAtLeast42(sprinklerPrefRepository.apiVersion());
    }

    public boolean hasSupportLogin() {
        return isAtLeast42(sprinklerPrefRepository.apiVersion());
    }

    public boolean hasParserFullFunctionality() {
        return isAtLeast41(sprinklerPrefRepository.apiVersion());
    }

    public boolean hasRainDelayMicroFunctionality() {
        return isApiAtLeast44();
    }

    public boolean hasSimulationFunctionality() {
        return isAtLeast42(sprinklerPrefRepository.apiVersion());
    }

    public boolean hasRestrictionsFunctionality() {
        return isAtLeastSpk2();
    }

    public boolean hasSpecialCondition() {
        return isApiAtLeast43();
    }

    public boolean hasDailyWaterNeedChart() {
        return isSpk1() && useNewApi();
    }

    public boolean hasDeviceUnits() {
        return isApiAtLeast44();
    }

    public boolean canUpdateHourlyRestriction() {
        return isApiAtLeast41();
    }

    /* Features visible in the UI */

    public boolean showVitalsChart() {
        return isAtLeastSpk2();
    }

    public boolean showCloudSetupDialog() {
        return useNewApi() && isSpk1() && isAtLeast364(sprinklerPrefRepository.softwareVersion());
    }

    public boolean hideFutureDaysForProgramCharts() {
        return isSpk1() && useNewApi() && isAtLeast364(sprinklerPrefRepository.softwareVersion());
    }

    public boolean showUsername() {
        return !useNewApi() || isLowerThan364(sprinklerPrefRepository.softwareVersion());
    }

    public boolean showMinutesSeconds() {
        return isAtLeast4x(sprinklerPrefRepository.softwareVersion());
    }

    public boolean showNextRun() {
        return isAtLeast4x(sprinklerPrefRepository.softwareVersion());
    }

    public boolean showWeather() {
        return isAtLeast4x(sprinklerPrefRepository.softwareVersion());
    }

    public boolean showWateringHistory() {
        return useNewApi() && isAtLeast364(sprinklerPrefRepository.softwareVersion());
    }

    public boolean showSnoozePhrasing() {
        return isAtLeast4x(sprinklerPrefRepository.softwareVersion());
    }

    public boolean showGraphs() {
        return useNewApi() && isAtLeast364(sprinklerPrefRepository.softwareVersion());
    }

    public boolean showRainSensor() {
        return isSpk2() || (isSpk3() && isAtLeast40864(sprinklerPrefRepository.softwareVersion()));
    }

    public boolean showExtraRainSensorFields() {
        return isApiAtLeast44();
    }

    public boolean showDiagnostics() {
        return isAtLeast4x(sprinklerPrefRepository.softwareVersion());
    }

    public boolean showFullAboutData() {
        return useNewApi() && isAtLeast364(sprinklerPrefRepository.softwareVersion());
    }

    public boolean showFullSettingsSubtitle() {
        return isAtLeast4x(sprinklerPrefRepository.softwareVersion());
    }

    public boolean showResetDefaults() {
        return isAtLeast4x(sprinklerPrefRepository.softwareVersion());
    }

    public boolean showReboot() {
        return isAtLeastSpk2();
    }

    public boolean showTimezone() {
        return isAtLeast4x(sprinklerPrefRepository.softwareVersion());
    }

    public boolean showDeviceName() {
        return isAtLeast4x(sprinklerPrefRepository.softwareVersion());
    }

    public boolean showLocationSettings() {
        return isAtLeast4x(sprinklerPrefRepository.softwareVersion());
    }

    public boolean showNetworkSettings() {
        return isAtLeast4x(sprinklerPrefRepository.softwareVersion());
    }

    public boolean showHourFormat() {
        return isAtLeast4x(sprinklerPrefRepository.softwareVersion());
    }

    public boolean showActiveRestrictionsScreen() {
        return isAtLeast42(sprinklerPrefRepository.apiVersion());
    }

    public boolean showRestoreBackup() {
        return hasBackup();
    }

    public boolean showAdvancedSettings() {
        return isAtLeastSpk2();
    }

    public boolean showMini8Settings() {
        return isSpk2();
    }

    public boolean showAllAdvancedZoneSettings() {
        return isApiAtLeast43();
    }

    public boolean showNewProgramDetailsScreen() {
        return isApiAtLeast43();
    }

    public boolean hasLiveModifiableTimer() {
        return isAtLeastSpk2();
    }

    public boolean showNotifications() {
        return isApiAtLeast42();
    }

    public boolean showBonjourService() {
        return isAtLeast40864(sprinklerPrefRepository.softwareVersion());
    }

    public boolean showWeatherOption() {
        return isAtLeastSpk2();
    }

    public boolean showExtraSoilTypes() {
        return isApiAtLeast44();
    }

    public boolean showOtherVegetationType() {
        return isSpk1();
    }

    /* Internal methods to check versions */

    private boolean isAtLeast40864(String version) {
        if (Strings.isBlank(version)) {
            return false;
        }
        String[] split = version.trim().split("\\.");
        if (split.length >= 3) {
            try {
                int major = Integer.parseInt(split[0].trim());
                int minor = Integer.parseInt(split[1].trim());
                int patch = Integer.parseInt(split[2].trim());
                return (major > 4 || (major == 4 && (minor > 0 || minor == 0 && patch >= 864)));
            } catch (NumberFormatException nfe) {
                return false;
            }
        } else if (split.length >= 2) {
            try {
                int major = Integer.parseInt(split[0].trim());
                int minor = Integer.parseInt(split[1].trim());
                return (major > 4 || (major == 4 && minor > 0));
            } catch (NumberFormatException nfe) {
                return false;
            }
        } else if (split.length == 1) {
            try {
                int major = Integer.parseInt(split[0].trim());
                return (major > 4);
            } catch (NumberFormatException nfe) {
                return false;
            }
        }
        return false;
    }

    private boolean isAtLeast364(String version) {
        if (Strings.isBlank(version)) {
            return false;
        }
        String[] split = version.trim().split("\\.");
        if (split.length >= 2) {
            try {
                int major = Integer.parseInt(split[0].trim());
                int minor = Integer.parseInt(split[1].trim());
                return (major > 3 || (major == 3 && minor >= 64));
            } catch (NumberFormatException nfe) {
                return false;
            }
        } else if (split.length == 1) {
            try {
                int major = Integer.parseInt(split[0].trim());
                return (major > 3);
            } catch (NumberFormatException nfe) {
                return false;
            }
        }
        return false;
    }

    private boolean is1x(String version) {
        if (Strings.isBlank(version)) {
            return false;
        }
        String[] split = version.trim().split("\\.");
        if (split.length >= 1) {
            try {
                int major = Integer.parseInt(split[0].trim());
                return (major == 1);
            } catch (NumberFormatException nfe) {
                return false;
            }
        }
        return false;
    }

    private boolean is2x(String version) {
        if (Strings.isBlank(version)) {
            return false;
        }
        String[] split = version.trim().split("\\.");
        if (split.length >= 1) {
            try {
                int major = Integer.parseInt(split[0].trim());
                return (major == 2);
            } catch (NumberFormatException nfe) {
                return false;
            }
        }
        return false;
    }

    private boolean is3x(String version) {
        if (Strings.isBlank(version)) {
            return false;
        }
        String[] split = version.trim().split("\\.");
        if (split.length >= 1) {
            try {
                int major = Integer.parseInt(split[0].trim());
                return (major == 3);
            } catch (NumberFormatException nfe) {
                return false;
            }
        }
        return false;
    }

    private boolean isSimulator(String version) {
        return "simulator".equalsIgnoreCase(version);
    }

    private boolean isAtLeast2x(String version) {
        if (Strings.isBlank(version)) {
            return false;
        }
        String[] split = version.trim().split("\\.");
        if (split.length >= 1) {
            try {
                int major = Integer.parseInt(split[0].trim());
                return (major >= 2);
            } catch (NumberFormatException nfe) {
                return false;
            }
        }
        return false;
    }

    private boolean isAtLeast4x(String version) {
        if (Strings.isBlank(version)) {
            return false;
        }
        String[] split = version.trim().split("\\.");
        if (split.length >= 1) {
            try {
                int major = Integer.parseInt(split[0].trim());
                return (major >= 4);
            } catch (NumberFormatException nfe) {
                return false;
            }
        }
        return false;
    }

    private boolean isAtLeast41(String version) {
        if (Strings.isBlank(version)) {
            return false;
        }
        String[] split = version.trim().split("\\.");
        if (split.length >= 2) {
            try {
                int major = Integer.parseInt(split[0].trim());
                int minor = Integer.parseInt(split[1].trim());
                return (major > 4 || (major == 4 && minor >= 1));
            } catch (NumberFormatException nfe) {
                return false;
            }
        }
        return false;
    }

    private boolean isAtLeast42(String version) {
        if (Strings.isBlank(version)) {
            return false;
        }
        String[] split = version.trim().split("\\.");
        if (split.length >= 2) {
            try {
                int major = Integer.parseInt(split[0].trim());
                int minor = Integer.parseInt(split[1].trim());
                return (major > 4 || (major == 4 && minor >= 2));
            } catch (NumberFormatException nfe) {
                return false;
            }
        }
        return false;
    }

    private boolean isAtLeast43(String version) {
        if (Strings.isBlank(version)) {
            return false;
        }
        String[] split = version.trim().split("\\.");
        if (split.length >= 2) {
            try {
                int major = Integer.parseInt(split[0].trim());
                int minor = Integer.parseInt(split[1].trim());
                return (major > 4 || (major == 4 && minor >= 3));
            } catch (NumberFormatException nfe) {
                return false;
            }
        }
        return false;
    }

    private boolean isAtLeast44(String version) {
        if (Strings.isBlank(version)) {
            return false;
        }
        String[] split = version.trim().split("\\.");
        if (split.length >= 2) {
            try {
                int major = Integer.parseInt(split[0].trim());
                int minor = Integer.parseInt(split[1].trim());
                return (major > 4 || (major == 4 && minor >= 4));
            } catch (NumberFormatException nfe) {
                return false;
            }
        }
        return false;
    }

    private boolean isLowerThan364(String version) {
        if (Strings.isBlank(version)) {
            return true;
        }
        String[] split = version.trim().split("\\.");
        if (split.length >= 2) {
            try {
                int major = Integer.parseInt(split[0].trim());
                int minor = Integer.parseInt(split[1].trim());
                return (major == 3 && minor < 64);
            } catch (NumberFormatException nfe) {
                return true;
            }
        }
        return true;
    }
}
