/// <reference path="../../typings/index.d.ts" />

import * as React from 'react';
import * as Material from 'material-ui';

interface IToolbarProps {

}

export class GripToolbar extends React.Component<IToolbarProps, {value: number}> {

  constructor(props: IToolbarProps) {
    super(props);
    this.state = {
      value: 3,
    };
  }

  public render(): React.ReactElement<{}> {
    return (
      <Material.Toolbar>
        <Material.ToolbarGroup firstChild={true}>
          <Material.DropDownMenu value={this.state.value} onChange={this.handleChange}>
            <Material.MenuItem value={1} primaryText='All Broadcasts'/>
            <Material.MenuItem value={2} primaryText='All Voice'/>
            <Material.MenuItem value={3} primaryText='All Text'/>
            <Material.MenuItem value={4} primaryText='Complete Voice'/>
            <Material.MenuItem value={5} primaryText='Complete Text'/>
            <Material.MenuItem value={6} primaryText='Active Voice'/>
            <Material.MenuItem value={7} primaryText='Active Text'/>
          </Material.DropDownMenu>
        </Material.ToolbarGroup>
      </Material.Toolbar>
    );
  }

  private handleChange = (event: any, index: any, value: any) => this.setState({value});
}
