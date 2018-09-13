import * as React from 'react';
import { connect } from 'react-redux';
import { Row, Col, Icon, Form, Input, Button } from 'antd';
import { withFormik, InjectedFormikProps, Formik, FormikProps } from 'formik';
import { createStructuredSelector } from 'reselect';

import logo from './logo_black.png';
import { login } from './actions';
import { StoreState, Login } from '../types';
import { isLoggingIn, loginError } from './selectors';

interface Props {
  onSubmit: (payload: Login) => void
  loggingIn: Boolean
  error: String
}

type FullProps = FormikProps<Login> & Props

const LoginForm = ({ handleSubmit, handleChange, values }: FullProps) => (
  <Form
    onSubmit={handleSubmit}
    className="LoginForm"
    layout="vertical">
    <Form.Item>
      <Input
        onChange={handleChange}
        prefix={<Icon type="user" style={{ color: 'rgba(0,0,0,.25)' }} />}
        placeholder="username" />
    </Form.Item>
    <Form.Item>
      <Input
        onChange={handleChange}
        prefix={<Icon type="lock" style={{ color: 'rgba(0,0,0,.25)' }} />}
        placeholder="password"
        type="password" />
    </Form.Item>
    <Form.Item>
      <Button
        type="primary"
        size="large"
        htmlType="submit">
        Log In
    </Button>
    </Form.Item>
  </Form>
);

const LoginFormRender = withFormik<Props, Login>({
  handleSubmit: (values, { props }) => props.onSubmit(values)
})(LoginForm);

const Login = (props: Props) => (
  <Row
    type="flex"
    justify="center"
    align="middle"
    style={{ minHeight: '100%' }}>
    <Col span={24}>
      <Row type="flex" justify="center">
        <Col span={10} style={{ textAlign: 'center' }}>
          <img src={logo} style={{ maxHeight: 250 }} alt="logo" />
        </Col>
      </Row>
      <Row type="flex" justify="center">
        <Col style={{ background: '#F0F3F5', padding: 15, textAlign: 'center' }} span={6}>
          <h1 className="Login-title">Please log in</h1>
          <LoginFormRender {...props} />
          {props.error && <div className="Login-error">{props.error}</div>}
        </Col>
      </Row>
    </Col>
  </Row>
);

const mapStateToProps = () =>
  createStructuredSelector({
    loggingIn: isLoggingIn(),
    error: loginError()
  });

const mapDispatchToProps = (dispatch: Function) => ({
  onSubmit: (payload: Login): void => dispatch(login(payload))
});

export default connect(mapStateToProps, mapDispatchToProps)(Login);
