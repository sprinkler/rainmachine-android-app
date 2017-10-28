package com.rainmachine.domain.boundary.data;

import com.rainmachine.domain.model.CloudSettings;
import com.rainmachine.domain.model.CurrentActiveRestrictions;
import com.rainmachine.domain.model.DevicePreferences;
import com.rainmachine.domain.model.GlobalRestrictions;
import com.rainmachine.domain.model.HourlyRestriction;
import com.rainmachine.domain.model.LoginStatus;
import com.rainmachine.domain.model.Parser;
import com.rainmachine.domain.model.Program;
import com.rainmachine.domain.model.Provision;
import com.rainmachine.domain.model.Update;
import com.rainmachine.domain.model.WateringQueueItem;
import com.rainmachine.domain.model.WifiSettingsSimple;
import com.rainmachine.domain.model.Zone;
import com.rainmachine.domain.model.ZoneProperties;
import com.rainmachine.domain.util.Irrelevant;

import org.javatuples.Pair;
import org.joda.time.LocalDateTime;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

public interface SprinklerRepository {

    Single<LoginStatus> login(final String pass, final boolean isRemember);

    Single<Boolean> testApiAuthenticated();

    Single<DevicePreferences> devicePreferences();

    Single<CloudSettings> cloudSettings();

    Completable enableCloud(boolean enable);

    Completable saveCloudEmail(String email);

    Completable saveZoneProperties(final ZoneProperties zoneProperties);

    Completable saveZonesProperties3(final ZoneProperties zoneProperties);

    Single<Integer> numberOfZones();

    Single<List<Program>> programs();

    Completable createProgram(Program program);

    Completable updateProgram(final Program program);

    Completable deleteProgram(long id);

    Single<Pair<List<Program>, Boolean>> programs3();

    Completable createUpdateProgram3(Program program, boolean use24HourFormat);

    Completable deleteProgram3(long id);

    Completable runAllParsers();

    Single<GlobalRestrictions> globalRestrictions();

    Completable saveGlobalRestrictionsRaw(final String body);

    Single<List<HourlyRestriction>> hourlyRestrictions();

    Completable saveHourlyRestriction(final HourlyRestriction restriction);

    Completable deleteHourlyRestriction(long restrictionId);

    Single<List<Parser>> parsers();

    Single<Provision> provision();

    Single<LocalDateTime> timeDate();

    Single<WifiSettingsSimple> wifiSettings();

    Single<CurrentActiveRestrictions> currentRestrictions();

    Single<List<WateringQueueItem>> wateringQueue();

    Single<List<Zone>> zones();

    Single<List<Zone>> zones3();

    Single<Zone> zone3(long zoneId);

    Single<Long> rainDelay();

    Single<Long> rainDelay3();

    Completable triggerUpdateCheck();

    Single<Irrelevant> triggerUpdate();

    Single<Update> update(boolean dealWithError);

    Single<Irrelevant> triggerUpdate3();

    Single<Update> update3(boolean dealWithError);

    Completable saveZoneDurations(List<Long> zoneDurations);
}
