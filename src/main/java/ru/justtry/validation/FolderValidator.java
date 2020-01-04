package ru.justtry.validation;

import com.google.common.base.Strings;
import org.springframework.stereotype.Component;
import ru.justtry.notes.NoteFolder;

import static ru.justtry.shared.ErrorMessages.*;

@Component
public class FolderValidator implements Validator
{
    @Override
    public void validate(Object object)
    {
        NoteFolder folder = (NoteFolder)object;

        checkName(folder);
        checkLevel(folder);

        // TODO Check if DB contains such folder with folderId
    }

    private void checkName(NoteFolder folder)
    {
        if (Strings.isNullOrEmpty(folder.getName()))
            throw new IllegalArgumentException(NAME_IS_NOT_SET);
    }

    private void checkLevel(NoteFolder folder)
    {
        if (folder.getLevel() == null)
            throw new IllegalArgumentException(LEVEL_IS_NOT_SET);

        if (folder.getLevel() < 1 || folder.getLevel() > 3)
            throw new IllegalArgumentException(LEVEL_RANGE_INCORRECT);
    }
}
