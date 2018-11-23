import { ViewElement } from './ViewElement';
import { Document as KKDocument } from './Document';

export class ButtonElement extends ViewElement {

    protected _enabled: boolean;

    constructor(document: KKDocument, name: string, id: number) {
        super(document, name, id);
        this._enabled = true;
        this._hoverStartTime = 20;
        this._hoverStayTime = 70;
        this.set("hover-class", "button-hover");
    }

    protected doEvent(event: Event, name: string, detail: any): void {
        if (this._enabled) {
            super.doEvent(event, name, detail);
        }
    }

    public set(key: string, value: string | undefined) {
        super.set(key, value);

        if (key == 'size') {
            if (value === undefined) {
                this._view.removeAttribute(key);
            } else {
                this._view.setAttribute(key, value);
            }
        } else if (key == 'disabled') {
            if (value == 'true' || value == 'disabled') {
                this._view.setAttribute(key, value);
                this._enabled = false;
            } else {
                this._view.removeAttribute(key);
                this._enabled = true;
            }
        } else if (key == 'type') {
            if (value === undefined) {
                this._view.removeAttribute(key);
            } else {
                this._view.setAttribute(key, value);
            }
        } else if (key == 'plain') {
            if (value == 'true' || value == 'plain') {
                this._view.setAttribute(key, value);
            } else {
                this._view.removeAttribute(key);
            }
        }
    }

}
