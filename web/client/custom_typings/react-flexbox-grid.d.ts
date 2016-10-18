/* tslint:disable */
declare module "react-flexbox-grid" {
  export import React = __React;

  interface ColProps extends React.Props<Col> {
    xs?: number|boolean;
    sm?: number|boolean;
    md?: number|boolean;
    lg?: number|boolean;
    xsOffset?: number;
    smOffset?: number;
    mdOffset?: number;
    lgOffset?: number;
    reverse?: boolean;
    className?: string;
    tagName?: string;
    children?: React.ReactNode;
  }

  export class Col extends React.Component<ColProps, {}> {
  }

  interface GridProps extends React.Props<Grid> {
    fluid?: boolean;
    className?: string;
    tagName?: string;
    children?: React.ReactNode;
  }

  export class Grid extends React.Component<GridProps, {}> {
  }

  interface RowProps extends React.Props<Row> {
    reverse?: boolean;
    start?: string;
    center?: string;
    end?: string;
    top?: string;
    middle?: string;
    bottom?: string;
    around?: string;
    between?: string;
    first?: string;
    last?: string;
    className?: string;
    tagName?: string;
    children?: React.ReactNode;
  }

  export class Row extends React.Component<RowProps, {}> {
  }
}
