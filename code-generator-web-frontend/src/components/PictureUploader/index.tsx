import { uploadFileUsingPost } from '@/services/backend/fileController';
import { LoadingOutlined, PlusOutlined } from '@ant-design/icons';
import { message, Upload, UploadProps } from 'antd';
import React, { useState } from 'react';
import { COS_HOST } from '@/constants';

interface Props {
  biz: string;
  onChange?: (url: string) => void;
  value?: string;
}

/**
 * 文件上传组件
 * @constructor
 */
const PictureUploader: React.FC<Props> = (props) => {
  const { biz, onChange, value } = props;
  const [loading, setLoading] = useState<boolean>(false);

  const uploadProps: UploadProps = {
    name: 'file',
    multiple: false,
    listType: "picture-card",
    maxCount: 1,
    showUploadList: false,
    disabled: loading,
    customRequest: async (fileObj: any) => {
      setLoading(true);
      try {
        const res = await uploadFileUsingPost({ biz }, {}, fileObj.file);
        const fullPath = COS_HOST + res.data;
        onChange?.(fullPath ?? '');
        fileObj.onSuccess(res.data);
      } catch (error: any) {
        message.error('上传失败' + error.message);
        fileObj.onError(error);
      }
      setLoading(false);
    },
  };

  const uploadButton = (
    <button style={{ border: 0, background: 'none' }} type="button">
      {loading ? <LoadingOutlined /> : <PlusOutlined /> }
      <div style={{ marginTop: 8 }}>上传</div>
    </button>
  )

  // @ts-ignore
  return (
    <Upload {...uploadProps}>
      { value ? <img src={value} alt="picture" style={{ width: '100%' }} /> : uploadButton }
    </Upload>
  );
};
export default PictureUploader;
