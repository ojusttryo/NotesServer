package ru.justtry.notes;

import lombok.Data;
import ru.justtry.shared.Identifiable;

@Data
public class NoteFolder extends Identifiable
{
    private String folderId;
    private String name;
    private Integer level;
}
