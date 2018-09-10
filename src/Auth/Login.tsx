import * as React from 'react';
import { connect } from 'react-redux';
import { Row, Col, Icon, Form, Input, Button } from 'antd';
import { withFormik } from 'formik';

import logo from './logo_black.png';
import { login } from './actions';
import { StoreState, Login } from '../types';

interface Props {
  onSubmit: Function
  loggingIn: Boolean
  error?: String
}

const Login = ({
  onSubmit,
  loggingIn,
  error,
}: Props) => {
  let errorBlock = <br />;

  if (error)
    errorBlock = <div className="Login-error">{error}</div>;

  return (
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
            <Form
              onSubmit={onSubmit}
              className="LoginForm"
              layout="vertical"
            >
              <Form.Item>
                <Input
                  prefix={<Icon type="user" style={{ color: 'rgba(0,0,0,.25)' }} />}
                  placeholder="username" />
              </Form.Item>
              <Form.Item>
                <Input
                  prefix={<Icon type="lock" style={{ color: 'rgba(0,0,0,.25)' }} />}
                  placeholder="password"
                  type="password" />)}
              </Form.Item>
              <Form.Item>
                <Button
                  type="primary"
                  size="large"
                  htmlType="submit"
                  loading={loggingIn}>
                  Log In
                </Button>
              </Form.Item>
            </Form>
            {errorBlock}
          </Col>
        </Row>
      </Col>
    </Row>
  );
};

const mapStateToProps = (state: StoreState) => state.auth;

const mapDispatchToProps = (dispatch: Function) => ({
  onSubmit: (payload: Login): void => dispatch(login(payload))
});

export default connect(mapStateToProps, mapDispatchToProps)(Login);
