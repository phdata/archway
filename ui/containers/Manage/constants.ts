export enum ManagePage {
  ComplianceTab = 'compliances',
  LinksTab = 'custom-links',
}

export enum OpenType {
  Add = 'add',
  Update = 'update',
  Delete = 'delete',
}

export const formLayout = {
  labelCol: {
    xs: { span: 24 },
    sm: { span: 4 },
  },
  wrapperCol: {
    xs: { span: 24 },
    sm: { span: 20 },
    lg: { span: 16 },
  },
};

export const linkLayout = {
  labelCol: {
    xs: { span: 24 },
    sm: { span: 3 },
  },
  wrapperCol: {
    xs: { span: 24 },
    md: { span: 21 },
    lg: { span: 15 },
  },
};
