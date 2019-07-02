import * as React from 'react';
import { connect } from 'react-redux';
import { Behavior } from '../../../components';
import { Col, Icon, Row, Tooltip, Button } from 'antd';
import { useDropzone } from 'react-dropzone';
import { createStructuredSelector } from 'reselect';

import { getCustomDescriptions } from '../../CustomWorkspaces/selectors';
import { Workspace } from '../../../models/Workspace';
import { CustomDescription } from '../../../models/Template';

interface Props {
  selected?: string;
  customDescriptions: CustomDescription[];
  onChange: (behavior: string) => void;
  importData: (jsonData: Workspace) => void;
}

const BehaviorPage = ({ selected, customDescriptions, onChange, importData }: Props) => {
  const { getRootProps, getInputProps, open } = useDropzone({
    // Disable click and keydown behavior
    noClick: true,
    noKeyboard: true,
    multiple: false,
    onDrop: files => handleImportClick(files),
  });
  const hasCustomDescriptions = customDescriptions.length > 0;

  function handleImportClick(files: any) {
    // tslint:disable-next-line: no-console
    if (typeof FileReader !== 'undefined') {
      const reader = new FileReader();

      reader.onabort = () => alert('file reading was aborted');
      reader.onerror = () => alert('file reading has failed');
      reader.onload = () => {
        // Do whatever you want with the file contents
        const binaryStr = reader.result;
        try {
          const workspaceReplica = JSON.parse(binaryStr as string);
          importData(workspaceReplica as Workspace);
        } catch (e) {
          alert('Not JSON file!');
        }
      };
      files.forEach((file: any) => reader.readAsBinaryString(file));
    } else {
      alert('File Read Failed. Please try a newer browser!');
    }
  }
  return (
    <div>
      <h3 style={{ display: 'inline-block', marginRight: 7 }}>What kind of behavior should we manage?</h3>
      <Tooltip
        /* tslint:disable:max-line-length */
        title="Heimdali will set up a structure that enables your team to work on a workspace in a certain way. See descriptions below for more information"
      >
        <Icon theme="twoTone" type="question-circle" />
      </Tooltip>
      <Row type="flex" justify="center" gutter={25} style={{ marginTop: 25, marginBottom: 25 }}>
        <Col span={12} lg={4} style={{ display: 'flex' }}>
          <Behavior
            behaviorKey="simple"
            selected={selected === 'simple'}
            onChange={(behavior, checked) => checked && onChange(behavior)}
            icon="team"
            title="Simple"
            description="A simple place for multiple users to collaborate on a solution."
            useCases={['brainstorming', 'evaluation', 'prototypes']}
          />
        </Col>
        <Col span={12} lg={4} style={{ display: 'flex' }}>
          <Behavior
            behaviorKey="structured"
            selected={selected === 'structured'}
            onChange={(behavior, checked) => checked && onChange(behavior)}
            icon="deployment-unit"
            title="Structured"
            description={`Data moves through various stages. Each stage represents a more "structured" version of the data.`}
            useCases={['publishings', 'data assets', 'external interfacing']}
          />
        </Col>
        {hasCustomDescriptions && (
          <Col span={12} lg={4} style={{ display: 'flex' }}>
            <Behavior
              behaviorKey=""
              selected={false}
              onChange={(behavior, checked) => checked && onChange(behavior)}
              icon="select"
              title="Custom"
              description={`Data moves through various stages. Each stage represents a more "structured" version of the data.`}
              useCases={['publishings', 'data assets', 'external interfacing']}
            />
          </Col>
        )}
      </Row>
      <Row type="flex" justify="center">
        <Col>
          <div {...getRootProps()}>
            <input {...getInputProps()} />
            <Button type="link" style={{ marginBottom: '50px' }} onClick={open}>
              Import a workspace
            </Button>
          </div>
        </Col>
      </Row>
    </div>
  );
};

const mapStateToProps = () =>
  createStructuredSelector({
    customDescriptions: getCustomDescriptions(),
  });

export default connect(
  mapStateToProps,
  null
)(BehaviorPage);
