<?php
declare(strict_types=1);

use App\Repository\CollectionRepository;
use App\Repository\CollectiontypeRepository;
use App\Repository\FieldRepository;
use App\Repository\FieldtypeRepository;
use App\Repository\ItemRepository;
use App\Repository\ItemdataRepository;
use App\Repository\RoleRepository;
use App\Repository\UserCollectionRepository;
use App\Repository\UserRepository;
use Doctrine\ORM\EntityManager;
use Slim\App;


return function (App $app) {
    $container = $app->getContainer();

    $entityManager = $container->get(EntityManager::class);

    $container->set(UserRepository::class, new UserRepository($entityManager));
    $container->set(CollectionRepository::class, new CollectionRepository($entityManager));
    $container->set(CollectiontypeRepository::class, new CollectiontypeRepository($entityManager));
    $container->set(FieldRepository::class, new FieldRepository($entityManager));
    $container->set(FieldtypeRepository::class, new FieldtypeRepository($entityManager));
    $container->set(ItemdataRepository::class, new ItemdataRepository($entityManager));
    $container->set(ItemRepository::class, new ItemRepository($entityManager));
    $container->set(RoleRepository::class, new RoleRepository($entityManager));
    $container->set(UserCollectionRepository::class, new UserCollectionRepository($entityManager));

};
