/// <reference path="../../typings/index.d.ts" />

import {OperationDescription} from 'grip-swagger';
import * as React from 'react';

interface IOperationProps {
  operationDescription: OperationDescription;
  createOperation: () => void;
}

export class Operation extends React.Component<IOperationProps, {}> {
  public render(): React.ReactElement<{}> {
    return (<div>Hello I'm bind!!!</div>);
  }
}
