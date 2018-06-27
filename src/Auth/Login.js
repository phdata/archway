import React from 'react';
import { connect } from 'react-redux';
import { Row, Col, Icon, Form, Input, Button } from 'antd';
import logo from './logo_black.png';
import { login, loginFieldChanged } from './actions';

const LoginForm = Form.create({
  onFieldsChange(props, changedFields) {
    props.onChange(changedFields);
  },
  mapPropsToFields(props) {
    return {
      username: Form.createFormField({
        value: props.loginForm.username,
      }),
      password: Form.createFormField({
        value: props.loginForm.password,
      }),
    };
  },
})(({
  form: {
    getFieldDecorator,
  },
  login,
  loggingIn,
}) => (
  <Form
    onSubmit={(e) => {
      e.preventDefault();
      login();
  }}
    className="LoginForm"
    layout="vertical"
  >
    <Form.Item>
      {getFieldDecorator('username', {
         rules: [{ required: true, message: 'Please provide your username.' }],
      })(<Input prefix={<Icon type="user" style={{ color: 'rgba(0,0,0,.25)' }} />} placeholder="username" />)}
    </Form.Item>
    <Form.Item>
      {getFieldDecorator('password', {
         rules: [{ required: true, message: 'Please provide your password.' }],
      })(<Input prefix={<Icon type="lock" style={{ color: 'rgba(0,0,0,.25)' }} />} placeholder="password" type="password" />)}
    </Form.Item>
    <Button type="primary" size="large" htmlType="submit" loading={loggingIn}>Log In</Button>
  </Form>
));


const Login = ({
  login,
  loginForm,
  loggingIn,
  error,
  loginFieldChanged,
}) => {
  let errorBlock = <br />;
  if (error) { errorBlock = <div className="Login-error">{error}</div>; }
  return (
    <Row type="flex" justify="center" align="middle" style={{ minHeight: '100%' }}>
      <Col span={24}>
        <Row type="flex" justify="center">
          <Col span={10} style={{ textAlign: 'center' }}>
            <img src={logo} style={{ maxHeight: 250 }} alt="logo" />
          </Col>
        </Row>
        <Row type="flex" justify="center">
          <Col style={{ background: '#F0F3F5', padding: 15, textAlign: 'center' }} span={6}>
            <h1 className="Login-title">Please log in</h1>
            <LoginForm
              loginForm={loginForm}
              loggingIn={loggingIn}
              login={login}
              onChange={loginFieldChanged}
            />
            {errorBlock}
          </Col>
        </Row>
      </Col>
    </Row>
  );
};

export default connect(
  state => state.auth,
  {
    login,
    loginFieldChanged,
  },
)(Login);
