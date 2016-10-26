/// <reference path="../../typings/index.d.ts" />

import * as React from 'react';
import List from 'material-ui/List';
import SubHeader from 'material-ui/Subheader';
import Divider from 'material-ui/Divider';
import {OperationDescription} from 'grip-swagger';
import {Operation} from './operation';
import {PropTypes} from 'react';
import {operationRoot, container} from './style.scss';
import {IOperationData} from './app';

interface IOperationListProps {
  operationData: IOperationData;
  createOperation: (name: string) => void;
}

export class OperationList extends React.Component<IOperationListProps, {}> {

  public static contextTypes: any = {
    muiTheme: PropTypes.object.isRequired,
  };

  public render(): React.ReactElement<{}> {
    console.log('Operation Root:', operationRoot);
    const operations: OperationDescription[] = this.props.operationData.operations || [];

    return (
      <div className={operationRoot}>
        <div className={container}>
          <List>
            <SubHeader>Operations</SubHeader>
            <div style={{height: parent.innerHeight - 48, overflow: 'scroll'}}>
              <div>
                {operations.map(
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
              </div>
            </div>
          </List>
        </div>
      </div>
    );
  }
}
