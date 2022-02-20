package ru.justtry.fileprocessing;

import static ru.justtry.database.DatabaseConfiguration.MONGO_MIGRATION_BEAN;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.justtry.database.Database;

@Component
@Slf4j
@RequiredArgsConstructor
@DependsOn({ MONGO_MIGRATION_BEAN })      // it should start after any changes have been made in migrations
public class FileCleaner
{
    private final Database database;

    @Value("${files.cleaner.enabled:false}")
    private boolean enabled;


    @Scheduled(fixedDelay = 3600_000)
    public void cleanFiles()
    {
        if (!enabled)
        {
            log.info("Removing old unused files is skipped due to settings.");
            return;
        }

        try
        {
            log.info("Removing old unused files...");
            long time = Instant.now().minus(1, ChronoUnit.HOURS).getEpochSecond();
            int count = database.deleteFilesOlderThan(time);
            log.info("Removed " + count + " files");
        }
        catch (Exception e)
        {
            log.error(e.toString());
        }
    }

}
