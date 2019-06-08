<?php
declare (strict_types = 1);

namespace App\Controller;

use Psr\Http\Message\ResponseInterface as Response;
use Psr\Container\ContainerInterface;
use App\Repository\FieldRepository;
use App\Repository\UserRepository;
use App\Repository\CollectionRepository;
use App\Repository\ItemRepository;
use App\Repository\ItemdataRepository;
use App\Entity\Item;
use App\Entity\Itemdata;
use App\Mappers\ItemMapper;

class ItemController
{
    protected $container;

    private $fieldRepo;
    private $userRepo;
    private $collectionRepo;
    private $itemRepo;
    private $itemDataRepo;
    private $itemMapper;

    // constructor receives container instance
    public function __construct(ContainerInterface $container)
    {
        $this->container = $container;

        $this->fieldRepo = $container->get(FieldRepository::class);
        $this->userRepo = $container->get(UserRepository::class);
        $this->collectionRepo = $container->get(CollectionRepository::class);
        $this->itemRepo = $container->get(ItemRepository::class);
        $this->itemDataRepo = $container->get(ItemdataRepository::class);

        $this->itemMapper = new ItemMapper($container);
    }

    public function addToCollection($request, $response, $args): Response
    {
        $collectionId = (int)$args['id'];
        $userId = (int)$request->getAttribute('userId');
        $input = $request->getParsedBody();

        $customFields = $this->fieldRepo->getCustomByCollectionId($collectionId);
        $basicFields = $this->fieldRepo->getBasicByCollectionId($collectionId);

        $allFields = array_merge($customFields, $basicFields);

        $keys = array_keys($input);

        $titleKey = '';

        foreach ($basicFields as $field) {
            if ($field->getName() == 'Title') {
                $titleKey = $field->getId() . "_0";
            }
        }

        $collections = array();

        array_push($collections, $this->collectionRepo->getById($collectionId));

        $newItem = new Item();
        $newItem->setName($input[$titleKey]);
        $newItem->setCreationdate(new \DateTime());
        $newItem->setAuthor($this->userRepo->getById($userId));
        $newItem->setActive(true);
        $newItem->setCollectionid($collections);
        $newItem = $this->itemRepo->save($newItem);

        foreach ($allFields as $field) {
            if ($field->getName() != 'Title') {
                $keysFound = array_filter($keys, function ($var) use ($field) {
                    return (stripos($var, $field->getId()) !== false);
                });

                if (count($keysFound) > 0) {
                    foreach ($keysFound as $key) {
                        $newItemData = new Itemdata();
                        $newItemData->setFieldvalue($input[$key]);
                        $newItemData->setItemid($newItem);
                        $newItemData->setFieldid($field);
                        $this->itemDataRepo->save($newItemData);
                    }
                }
            }
        }

        return $response;
    }

    public function getItemFromCollection($request, $response, $args): Response
    {
        $collectionId = (int)$args['id'];
        $page = (int)$args['page'];
        $itemsOnPage = (int)$args['itemsOnPage'];
        $userId = (int)$request->getAttribute('userId');

        $items = $this->itemRepo->getItemsByCollection($collectionId, $page, $itemsOnPage);

        $returnValue = array();

        foreach($items as $item)
        {
            array_push($returnValue, $this->itemMapper->mapItemToDto($item));
        }

        $response->getBody()->write(json_encode($returnValue));
        return  $response->withHeader('Content-Type', 'application/json');
    }
}
