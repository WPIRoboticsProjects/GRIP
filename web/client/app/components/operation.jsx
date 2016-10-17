/// <reference path="../../typings/index.d.ts" />
import * as React from 'react';
import { ListItem } from 'material-ui/List';
export class Operation extends React.Component {
    render() {
        return (<ListItem primaryText={this.props.operationDescription.name}/>);
    }
}
