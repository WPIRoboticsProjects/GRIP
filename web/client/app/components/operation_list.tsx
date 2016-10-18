/// <reference path="../../typings/index.d.ts" />

import * as React from 'react';
import List from 'material-ui/List';
import SubHeader from 'material-ui/Subheader';
import Divider from 'material-ui/Divider';
import {OperationDescription} from 'grip-swagger';
import {Operation} from './operation';
import {PropTypes} from 'react';
import {operationRoot} from './style.scss';

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
    const {prepareStyles}: any = (this.context as any).muiTheme;

    const custStyles: any = {
      container: {
        border: 'solid 1px #d9d9d9',
        height: '500px',
        overflow: 'hidden',
      },
    };

    return (
      <div className={operationRoot}>
        <div style={prepareStyles(custStyles.container)}>
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
