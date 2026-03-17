import { useCallback, useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
  Button,
  Card,
  Descriptions,
  Form,
  InputNumber,
  Modal,
  message,
} from "antd";
import axios from "axios";
import NavBar from "../../component/NavBar/NavBar";
import type { TraderAccountView } from "../../types/trading";
import { depositFundsUrl, traderAccountUrl, withdrawFundsUrl } from "../../util/constants";
import "./TraderDetails.scss";

export default function TraderDetails() {
  const { traderId } = useParams<{ traderId: string }>();
  const navigate = useNavigate();
  const id = Number(traderId);

  const [loading, setLoading] = useState(false);
  const [profile, setProfile] = useState<TraderAccountView | null>(null);
  const [isDepositModalVisible, setIsDepositModalVisible] = useState(false);
  const [isWithdrawModalVisible, setIsWithdrawModalVisible] = useState(false);
  const [depositForm] = Form.useForm<{ amount: number }>();
  const [withdrawForm] = Form.useForm<{ amount: number }>();

  const api = useMemo(
    () =>
      axios.create({
        headers: { "Content-Type": "application/json" },
      }),
    []
  );

  const getProfile = useCallback(async () => {
    if (!Number.isFinite(id)) {
      setProfile(null);
      return;
    }
    setLoading(true);
    try {
      const response = await api.get<TraderAccountView>(`${traderAccountUrl}${id}`);
      setProfile(response.data);
    } catch (err) {
      message.error("Failed to load trader profile");
      setProfile(null);
    } finally {
      setLoading(false);
    }
  }, [api, id]);

  useEffect(() => {
    void getProfile();
  }, [getProfile]);

  const onDeposit = async (values: { amount: number }) => {
    setLoading(true);
    try {
      await api.put(`${depositFundsUrl}${id}/amount/${values.amount}`, {});
      message.success("Deposit successful");
      setIsDepositModalVisible(false);
      depositForm.resetFields();
      await getProfile();
    } catch (err) {
      message.error("Deposit failed");
    } finally {
      setLoading(false);
    }
  };

  const onWithdraw = async (values: { amount: number }) => {
    setLoading(true);
    try {
      await api.put(`${withdrawFundsUrl}${id}/amount/${values.amount}`, {});
      message.success("Withdraw successful");
      setIsWithdrawModalVisible(false);
      withdrawForm.resetFields();
      await getProfile();
    } catch (err) {
      message.error("Withdraw failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="trader-details">
      <NavBar />
      <div className="trader-details-content">
        <div className="title">
          <span>Trader Account</span>
          <Button onClick={() => navigate("/dashboard")}>Back</Button>
        </div>

        <div className="trader-cards">
          <Card className="trader-card" loading={loading}>
            {profile ? (
              <Descriptions size="small" column={2}>
                <Descriptions.Item label="First Name">{profile.firstName}</Descriptions.Item>
                <Descriptions.Item label="Last Name">{profile.lastName}</Descriptions.Item>
                <Descriptions.Item label="Email" span={2}>
                  {profile.email}
                </Descriptions.Item>
                <Descriptions.Item label="Date of Birth">{profile.dob}</Descriptions.Item>
                <Descriptions.Item label="Country">{profile.country}</Descriptions.Item>
              </Descriptions>
            ) : (
              <div className="placeholder">
                {Number.isFinite(id) ? "No profile found." : "Invalid trader id."}
              </div>
            )}
          </Card>

          <Card className="trader-card amount-card" loading={loading}>
            <div className="amount-heading">Amount</div>
            <div className="amount-value">{profile?.amount ?? 0}$</div>
          </Card>

          <div className="actions">
            <Button
              type="primary"
              onClick={() => setIsDepositModalVisible(true)}
              disabled={!profile}
            >
              Deposit Funds
            </Button>
            <Button
              onClick={() => setIsWithdrawModalVisible(true)}
              disabled={!profile}
            >
              Withdraw Funds
            </Button>
          </div>
        </div>
      </div>

      <Modal
        title="Deposit Funds"
        okText="Submit"
        visible={isDepositModalVisible}
        onOk={() => depositForm.submit()}
        onCancel={() => {
          setIsDepositModalVisible(false);
          depositForm.resetFields();
        }}
        confirmLoading={loading}
        destroyOnClose
      >
        <Form form={depositForm} layout="vertical" onFinish={onDeposit}>
          <Form.Item
            label="Amount"
            name="amount"
            rules={[
              { required: true, message: "Amount is required" },
              { type: "number", min: 0.01, message: "Amount must be greater than 0" },
            ]}
          >
            <InputNumber min={0.01} step={0.01} style={{ width: "100%" }} placeholder="12.34" />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="Withdraw Funds"
        okText="Submit"
        visible={isWithdrawModalVisible}
        onOk={() => withdrawForm.submit()}
        onCancel={() => {
          setIsWithdrawModalVisible(false);
          withdrawForm.resetFields();
        }}
        confirmLoading={loading}
        destroyOnClose
      >
        <Form form={withdrawForm} layout="vertical" onFinish={onWithdraw}>
          <Form.Item
            label="Amount"
            name="amount"
            rules={[
              { required: true, message: "Amount is required" },
              { type: "number", min: 0.01, message: "Amount must be greater than 0" },
            ]}
          >
            <InputNumber min={0.01} step={0.01} style={{ width: "100%" }} placeholder="12.34" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
