import * as React from 'react';
import { Button, Form, Icon, Input } from 'antd';
import { InjectedFormikProps, withFormik } from 'formik';
import { Login } from '../../../models/Login';

interface Props {
  onSubmit: (payload: Login) => void;
  loggingIn: boolean;
  error: string;
}

class LoginForm extends React.PureComponent<InjectedFormikProps<Props, Login>> {
  public render() {
    const {
      handleSubmit,
      handleChange,
      values: { username, password },
    } = this.props;
    return (
      <Form onSubmit={handleSubmit} className="LoginForm" layout="vertical">
        <Form.Item>
          <Input
            name="username"
            onChange={handleChange}
            size="large"
            value={username}
            prefix={<Icon type="user" />}
            placeholder="username"
          />
        </Form.Item>
        <Form.Item>
          <Input
            name="password"
            onChange={handleChange}
            size="large"
            value={password}
            prefix={<Icon type="lock" />}
            placeholder="password"
            type="password"
          />
        </Form.Item>
        <Form.Item>
          <Button type="primary" size="large" htmlType="submit">
            Log In
          </Button>
        </Form.Item>
      </Form>
    );
  }
}

const LoginFormRender = withFormik<Props, Login>({
  handleSubmit: (values, { props }) => props.onSubmit(values),
})(LoginForm);

export default LoginFormRender;
