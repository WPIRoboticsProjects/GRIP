/// <reference path="../../typings/index.d.ts" />

import {OperationDescription} from 'grip-swagger';
import * as React from 'react';
interface IOperationListProps {
  operationDescriptions: OperationDescription[];
  createOperation: (name: string) => void;
}

export class OperationList extends React.Component<IOperationListProps, {}> {
  public render(): React.ReactElement<{}> {
    return (<div>Test</div>);
  }
}
