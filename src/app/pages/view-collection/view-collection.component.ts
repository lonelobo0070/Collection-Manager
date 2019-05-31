import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, ParamMap } from '@angular/router';
import { CollectionService } from '../../Services/collection.service';
import { CustomField } from '../../Entities/custom-field';
import { Collection } from '../../Entities/collection';
import { faList, faTh } from '@fortawesome/free-solid-svg-icons';
import { ItemService } from '../../Services/item.service';
import { Item } from '../../Entities/item';

@Component({
  selector: 'ngx-view-collection',
  templateUrl: './view-collection.component.html',
  styleUrls: ['./view-collection.component.scss'],
})
export class ViewCollectionComponent implements OnInit {

  listIcon = faList;
  cardIcon = faTh;

  itemsPerPage: number = 50;
  currentPage: number = 0;
  currentView: string = 'list';
  currentLetterFilter: string = 'ALL';

  firstLetterFilter: string[] ;

  collection: Collection;
  fields: CustomField[];
  items: Item[];

  constructor(private route: ActivatedRoute, private collectionService: CollectionService,
    private itemService: ItemService) { }

  ngOnInit() {

    this.firstLetterFilter = '#ABCDEFGHIJKLMNOPQRSTUVWQYZ'.split('');

    this.route.paramMap.subscribe((params: ParamMap) => {
      if (params.has('id')) {

        this.collectionService.getUserCollection(+params.get('id')).subscribe(data => {
          this.collection = data;
          this.fields = data.fields;
        });

        // this.collection = this.collectionService.getUserCollection(+params.get('id'));
        // this.fields = this.customFieldService.getFieldsByCollection(+params.get('id'));

        this.items = this.itemService.getItemOfCollection(this.collection.id, this.currentPage, this.itemsPerPage);
      }
    });
  }

  changeView(view: string): void {
    this.currentView = view;
  }

  changeLetterFilter(filter: string): void {
    this.currentLetterFilter = filter;
  }

  onScroll() {
    this.currentPage += 1;

    const newItems: Item[] = this.itemService.getItemOfCollection(this.collection.id, this.currentPage, this.itemsPerPage);

    for (const item of newItems)
    {
      this.items.push(item);
    }
  }

}
