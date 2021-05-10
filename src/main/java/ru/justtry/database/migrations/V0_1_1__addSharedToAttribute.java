package ru.justtry.database.migrations;

import static com.mongodb.client.model.Filters.exists;
import static ru.justtry.shared.AttributeConstants.ATTRIBUTES_COLLECTION;
import static ru.justtry.shared.AttributeConstants.SHARED;

import java.util.List;

import org.bson.Document;
import org.slf4j.MDC;

import com.github.ojusttryo.migmong.migration.MigrationContext;
import com.github.ojusttryo.migmong.migration.annotations.Migration;
import com.github.ojusttryo.migmong.migration.annotations.MigrationUnit;

import lombok.extern.slf4j.Slf4j;
import ru.justtry.database.Database;
import ru.justtry.shared.AttributeConstants;

@Migration
@Slf4j
public class V0_1_1__addSharedToAttribute
{
    @MigrationUnit(id = 1)
    public void addFieldSharedToAttribute(MigrationContext context)
    {
        MDC.put("migration", V0_1_1__addSharedToAttribute.class.getSimpleName());

        Database db = context.getApplicationContext().getBean(Database.class);

        List<Document> attributeDocuments = db.getDocuments(ATTRIBUTES_COLLECTION, exists(SHARED, false), null);
        for (Document document : attributeDocuments)
        {
            MDC.put("attribute", document.get(AttributeConstants.NAME).toString());

            document.put(SHARED, true);
            db.updateDocument(ATTRIBUTES_COLLECTION, document);

            log.info("The field 'shared' has been added to the attribute");
        }

        MDC.remove("migration");
        MDC.remove("attribute");
    }
}
