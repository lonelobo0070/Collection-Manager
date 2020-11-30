package be.jimmyd.cm.domain.logic;

import be.jimmyd.cm.dto.ItemDto;
import be.jimmyd.cm.entities.*;
import be.jimmyd.cm.repositories.*;
import liquibase.pro.packaged.I;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class ItemLogic {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final FieldRepository fieldRepository;
    private final CollectionRepository collectionRepository;
    private final ItemDataRepository itemDataRepository;

    public ItemLogic(final UserRepository userRepository, final ItemRepository itemRepository, final FieldRepository fieldRepository,
                     final CollectionRepository collectionRepository, final ItemDataRepository itemDataRepository) {
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.fieldRepository = fieldRepository;
        this.collectionRepository = collectionRepository;
        this.itemDataRepository = itemDataRepository;
    }

    @Transactional
    public void addItemToCollection(long collectionId, Map<String, String> itemData, String userMail) {

        final User user = userRepository.findByMail(userMail);
        final Optional<Collection> collectionOptional = collectionRepository.findById(collectionId);

        //TODo check user permission on collection
        //TODO split this method

        if (collectionOptional.isPresent()) {

            final List<Field> fields = fieldRepository.findBasicFieldByCollectionId(collectionId);
            fields.addAll(fieldRepository.findCustomFieldByCollectionId(collectionId));

            long titleFieldId = fields.stream().filter(n -> n.getName().equalsIgnoreCase("Title")).map(n -> n.getId()).findFirst().get();
            long coverFieldId = fields.stream().filter(n -> n.getName().equalsIgnoreCase("Cover")).map(n -> n.getId()).findFirst().get();

            String title = itemData.remove(titleFieldId + "_0");
            String cover = itemData.remove(coverFieldId + "_0");

            Item newItem = new Item();
            newItem.setActive(true);
            newItem.setName(title);
            newItem.setAuthor(user);
            newItem.setCreationDate(LocalDateTime.now());
            newItem.setImage(cover);
            newItem.setLastModified(LocalDateTime.now());

            newItem.setCollections(List.of(collectionOptional.get()));

            final Item finalNewItem = itemRepository.save(newItem);

            itemData.forEach((key, value) -> {

                long fieldId = getFieldIDFromKey(key);

                fields.stream().filter(field -> field.getId() == fieldId).findFirst().ifPresent(field -> {
                            Itemdata itemdata = new Itemdata();
                            itemdata.setField(field);
                            itemdata.setFieldValue(value);
                            itemdata.setItem(finalNewItem);
                            //TODO add validation on field level (required fields, ...)

                            itemDataRepository.save(itemdata);
                        }
                );
            });

        }
    }

    private long getFieldIDFromKey(String key) {
        return Long.valueOf(key.substring(0, key.indexOf("_")));
    }
}
