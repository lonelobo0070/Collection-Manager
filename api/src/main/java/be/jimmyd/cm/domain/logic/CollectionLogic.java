package be.jimmyd.cm.domain.logic;

import be.jimmyd.cm.domain.mappers.CollectionMapper;
import be.jimmyd.cm.domain.mappers.FieldMapper;
import be.jimmyd.cm.dto.CollectionDto;
import be.jimmyd.cm.dto.FieldDto;
import be.jimmyd.cm.entities.*;
import be.jimmyd.cm.repositories.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class CollectionLogic {

    private final CollectionRepository collectionRepository;
    private final UserRepository userRepository;
    private final CollectionMapper collectionMapper;
    private final FieldRepository fieldRepository;
    private final UserCollectionLogic userCollectionLogic;
    private final CollectionTypeRepository collectionTypeRepository;
    private final FieldMapper fieldMapper;
    private final FieldTypeRepository fieldTypeRepository;

    public CollectionLogic(CollectionRepository collectionRepository,  UserRepository userRepository, FieldRepository fieldRepository,
                           final UserCollectionLogic userCollectionLogic, final CollectionTypeRepository collectionTypeRepository,
                           final FieldTypeRepository fieldTypeRepository) {
        this.collectionRepository = collectionRepository;
        this.userRepository = userRepository;
        this.collectionMapper = CollectionMapper.INSTANCE;
        this.fieldRepository = fieldRepository;
        this.userCollectionLogic = userCollectionLogic;
        this.collectionTypeRepository = collectionTypeRepository;
        this.fieldMapper = FieldMapper.INSTANCE;
        this.fieldTypeRepository = fieldTypeRepository;
    }

    public List<CollectionDto> getByUser(String mail) {
        final User user = userRepository.findByMail(mail);

        final List<Collection> collections = collectionRepository.getByUser(user.getId());

        return collectionMapper.collectionToDto(collections);
    }

    public CollectionDto getById(long collectionId) {

        //TODO add check for user permission
        final Optional<Collection> collection = collectionRepository.findById(collectionId);

        if(collection.isPresent()) {
            return collectionMapper.collectionToDto(collection.get());
        }

        return null;

    }

    public void deleteById(long collectionId) {
        collectionRepository.findById(collectionId).ifPresent(collection -> {
            collectionRepository.delete(collection);
        });
    }

    @Transactional
    public void createCollection(CollectionDto collectionDto, String mail) {

        final CollectionType type = collectionTypeRepository.getByName(collectionDto.type);

        Collection collection = new Collection();
        collection.setName(collectionDto.getName());
        collection.setActive(true);
        collection.setType(type);

        collection.setFields(new ArrayList<>());

        for(FieldDto dto : collectionDto.getFields()) {
            final Field field = fieldMapper.dtoToField(dto);
            final FieldType fieldType = fieldTypeRepository.findbyName(dto.getType());
            field.setType(fieldType);

            collection.getFields().add(field);
        }

        Collection savedCollection = collectionRepository.save(collection);

        userCollectionLogic.addUserToCollection(mail, "Owner", savedCollection);

    }
}
