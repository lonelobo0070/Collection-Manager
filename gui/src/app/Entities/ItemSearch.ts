import {Directive, OnInit} from '@angular/core';

@Directive()
export class ItemSearchDirective implements OnInit {

  constructor(
    public externalId: string,
    public name: string,
    public image: string,
    public releaseDate: string,
    public source: string,
    public url: string) {
  }

  ngOnInit(): void {
  }
}
