/// <reference path="../../typings/index.d.ts" />

import {OperationDescription} from 'grip-swagger';
import * as React from 'react';
import {ListItem} from 'material-ui/List';

interface IOperationProps extends React.Props<{}> {
  operationDescription: OperationDescription;
  createOperation: () => void;
}

export class Operation extends React.Component<IOperationProps, {}> {
  public render(): React.ReactElement<{}> {
    return (
      <ListItem primaryText={
        this.props.operationDescription.name
      }/>
    );
  }
}
