/// <reference path="../../typings/index.d.ts" />
import * as React from 'react';
import * as Material from 'material-ui';
export class GripToolbar extends React.Component {
    constructor(props) {
        super(props);
        this.handleChange = (event, index, value) => this.setState({ value });
        this.state = {
            value: 3,
        };
    }
    render() {
        return (<Material.Toolbar>
        <Material.ToolbarTitle text='GRIP'/>
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
      </Material.Toolbar>);
    }
}
