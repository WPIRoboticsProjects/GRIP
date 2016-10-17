/// <reference path="../../typings/index.d.ts" />
import * as React from 'react';
import List from 'material-ui/List';
import SubHeader from 'material-ui/Subheader';
import Divider from 'material-ui/Divider';
import { Operation } from './operation';
export class OperationList extends React.Component {
    render() {
        return (<div>
        <List>
          <SubHeader>Operations</SubHeader>
          {this.props.operationDescriptions.map((value, index) => {
            return (<div key={index}>
                  <Operation createOperation={() => this.props.createOperation(value.name)} operationDescription={value}/>
                  <Divider inset={true}/>
                </div>);
        })}
        </List>
      </div>);
    }
}
