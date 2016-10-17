/// <reference path="../../typings/index.d.ts" />

import * as React from 'react';
import List from 'material-ui/List';
import SubHeader from 'material-ui/Subheader';
import Divider from 'material-ui/Divider';
import {OperationDescription} from 'grip-swagger';
import {Operation} from './operation';

interface IOperationListProps {
  operationDescriptions: OperationDescription[];
  createOperation: (name: string) => void;
}

export class OperationList extends React.Component<IOperationListProps, {}> {
  public render(): React.ReactElement<{}> {
    return (
      <div>
        <List>
          <SubHeader>Operations</SubHeader>
          {this.props.operationDescriptions.map(
            (value: OperationDescription, index: number) => {
              return (
                <div key={index}>
                  <Operation
                    createOperation={() => this.props.createOperation(value.name)}
                    operationDescription={value}/>
                  <Divider inset={true}/>
                </div>
              );
            })
          }
        </List>
      </div>);
  }
}
