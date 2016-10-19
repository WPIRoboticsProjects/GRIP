/// <reference path="../../typings/index.d.ts" />

import * as React from 'react';
import List from 'material-ui/List';
import SubHeader from 'material-ui/Subheader';
import Divider from 'material-ui/Divider';
import {OperationDescription} from 'grip-swagger';
import {Operation} from './operation';
import {PropTypes} from 'react';
import {operationRoot, container} from './style.scss';

interface IOperationListProps {
  operationDescriptions: OperationDescription[];
  createOperation: (name: string) => void;
}

export class OperationList extends React.Component<IOperationListProps, {}> {

  public static contextTypes: any = {
    muiTheme: PropTypes.object.isRequired,
  };

  public render(): React.ReactElement<{}> {
    console.log('Operation Root:', operationRoot);
    // const {prepareStyles}: any = (this.context as any).muiTheme;

    return (
      <div className={operationRoot}>
        <div className={container}>
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
        </div>
      </div>
    );
  }
}
