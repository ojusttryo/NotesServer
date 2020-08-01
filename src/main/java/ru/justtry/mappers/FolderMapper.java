package ru.justtry.mappers;

import static ru.justtry.shared.Constants.MONGO_ID;
import static ru.justtry.shared.Constants.NAME;
import static ru.justtry.shared.FolderConstants.LEVEL;
import static ru.justtry.shared.NoteConstants.FOLDER_ID;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

import ru.justtry.notes.NoteFolder;

@Component
public class FolderMapper extends Mapper
{
    @Override
    public Object getObject(Document document)
    {
        NoteFolder folder = new NoteFolder();

        folder.setId(document.get(MONGO_ID).toString());
        folder.setName(document.get(NAME).toString());
        folder.setFolderId(getStringOrNull(document, FOLDER_ID));
        folder.setLevel((Integer)document.get(LEVEL));

        return folder;
    }


    @Override
    public Document getDocument(Object object)
    {
        NoteFolder folder = (NoteFolder)object;

        Document document = new Document()
                .append(NAME, folder.getName())
                .append(FOLDER_ID, folder.getFolderId())
                .append(LEVEL, folder.getLevel());

        if (!Strings.isNullOrEmpty(folder.getId()))
            document.append(MONGO_ID, new ObjectId(folder.getId()));

        return document;
    }
}
