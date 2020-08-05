package ru.justtry.validation;

import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

import ru.justtry.notes.NoteFolder;
import ru.justtry.shared.ErrorMessages;

@Component
public class FolderValidator implements Validator
{
    @Override
    public void validate(Object object, String collectionName)
    {
        NoteFolder folder = (NoteFolder)object;

        checkName(folder);
        checkLevel(folder);

        // TODO Check if DB contains such folder with folderId
    }

    private void checkName(NoteFolder folder)
    {
        if (Strings.isNullOrEmpty(folder.getName()))
            throw new IllegalArgumentException(ErrorMessages.getIsNotSet("Name"));
    }

    private void checkLevel(NoteFolder folder)
    {
        if (folder.getLevel() == null)
            throw new IllegalArgumentException(ErrorMessages.getIsNotSet("Level"));

        if (folder.getLevel() < 1 || folder.getLevel() > 3)
            throw new IllegalArgumentException("Level must be between 1 and 3");
    }
}
