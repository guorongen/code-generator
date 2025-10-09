import { COS_HOST } from '@/constants';
import {
  testDownloadFileUsingGet,
  testUploadFileUsingPost,
} from '@/services/backend/fileController';
import { InboxOutlined } from '@ant-design/icons';
import { Button, Card, Divider, Flex, message, Upload, UploadProps } from 'antd';
import React, { useState } from 'react';
import {saveAs} from "file-saver";

const { Dragger } = Upload;

/**
 * 文件上传下载测试页面
 * @constructor
 */
const TestFilePage: React.FC = () => {
  const [value, setValue] = useState<string>();

  const props: UploadProps = {
    name: 'file',
    multiple: false,
    maxCount: 1,
    customRequest: async (fileObj: any) => {
      try {
        const res = await testUploadFileUsingPost({}, fileObj.file);
        fileObj.onSuccess(res.data);
        setValue(res.data);
      } catch (error: any) {
        message.error('上传失败' + error.message);
        fileObj.onError(error);
      }
    },
    onRemove() {
      setValue(undefined);
    },
  };

  // @ts-ignore
  return (
    <Flex gap={16}>
      <Card title="文件上传">
        <Dragger {...props}>
          <p className="ant-upload-drag-icon">
            <InboxOutlined />
          </p>
          <p className="ant-upload-text">Click or drag file to this area to upload</p>
          <p className="ant-upload-hint">
            Support for a single or bulk upload. Strictly prohibited from uploading company data or
            other banned files.
          </p>
        </Dragger>
      </Card>
      <Card title="文件下载">
        <div>文件地址：{COS_HOST + value}</div>
        <Divider />
        <img src={COS_HOST + value} height={200} />
        <Divider />
        <Button
          onClick={async () => {
            const blob = await testDownloadFileUsingGet(
              { filepath: value },
              { responseType: 'blob' },
            );

            const fullPath = COS_HOST + value;
            saveAs(blob, fullPath.substring(fullPath.lastIndexOf("/") + 1))
          }}
        >
          点击下载文件
        </Button>
      </Card>
    </Flex>
  );
};
export default TestFilePage;
