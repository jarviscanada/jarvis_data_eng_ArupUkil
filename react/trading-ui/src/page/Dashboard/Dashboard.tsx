import { useCallback, useEffect, useMemo, useState } from "react";
import { Button, DatePicker, Form, Input, Modal, message } from "antd";
import type { Moment } from "moment";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import NavBar from "../../component/NavBar/NavBar";
import TraderList from "../../component/TraderList/TraderList";
import type { Trader } from "../../types/trading";
import {
  createTraderUrl,
  deleteTraderUrl,
  tradersUrl,
} from "../../util/constants";
import "./Dashboard.scss";

type CreateTraderForm = {
  firstName: string;
  lastName: string;
  email: string;
  country: string;
  dob: Moment;
};

const namePattern = /^[a-zA-Z][a-zA-Z\s'-]*$/;
const countryPattern = /^[a-zA-Z][a-zA-Z\s'-]*$/;

export default function Dashboard() {
  const navigate = useNavigate();
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [loading, setLoading] = useState(false);
  const [traders, setTraders] = useState<Trader[]>([]);
  const [form] = Form.useForm<CreateTraderForm>();

  const api = useMemo(
    () =>
      axios.create({
        headers: { "Content-Type": "application/json" },
      }),
    []
  );

  const getTraders = useCallback(async () => {
    setLoading(true);
    try {
      const response = await api.get<Trader[]>(tradersUrl);
      setTraders(response.data || []);
    } catch (err) {
      message.error("Failed to load traders");
    } finally {
      setLoading(false);
    }
  }, [api]);

  useEffect(() => {
    void getTraders();
  }, [getTraders]);

  const onTraderDelete = async (id: number) => {
    setLoading(true);
    try {
      await api.delete(`${deleteTraderUrl}/${id}`);
      message.success(`Deleted trader ${id}`);
      await getTraders();
    } catch (err) {
      message.error("Failed to delete trader (balance must be 0 and no open positions)");
    } finally {
      setLoading(false);
    }
  };

  const onCreateTraderSubmit = async (values: CreateTraderForm) => {
    setLoading(true);
    try {
      const dob = values.dob.format("YYYY-MM-DD");
      const paramUrl = `/firstname/${encodeURIComponent(
        values.firstName
      )}/lastname/${encodeURIComponent(values.lastName)}/dob/${encodeURIComponent(
        dob
      )}/country/${encodeURIComponent(values.country)}/email/${encodeURIComponent(
        values.email
      )}`;

      await api.post(`${createTraderUrl}${paramUrl}`, {});
      message.success("Trader created");
      setIsModalVisible(false);
      form.resetFields();
      await getTraders();
    } catch (err) {
      message.error("Failed to create trader");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="dashboard">
      <NavBar />
      <div className="dashboard-content">
        <div className="title">
          <span>Dashboard</span>
          <Button type="primary" onClick={() => setIsModalVisible(true)}>
            Add New Trader
          </Button>
        </div>

        <TraderList
          traders={traders}
          loading={loading}
          onTraderDeleteClick={onTraderDelete}
          onTraderRowClick={(id) => navigate(`/trader/${id}`)}
        />
      </div>

      <Modal
        title="Add New Trader"
        okText="Submit"
        visible={isModalVisible}
        onOk={() => form.submit()}
        onCancel={() => {
          setIsModalVisible(false);
          form.resetFields();
        }}
        confirmLoading={loading}
        destroyOnClose
      >
        <Form<CreateTraderForm>
          form={form}
          layout="vertical"
          onFinish={onCreateTraderSubmit}
        >
          <div className="add-trader-form">
            <div className="add-trader-field">
              <Form.Item
                label="First Name"
                name="firstName"
                rules={[
                  { required: true, message: "First name is required" },
                  { pattern: namePattern, message: "Enter a valid first name" },
                ]}
              >
                <Input placeholder="John" />
              </Form.Item>
            </div>
            <div className="add-trader-field">
              <Form.Item
                label="Last Name"
                name="lastName"
                rules={[
                  { required: true, message: "Last name is required" },
                  { pattern: namePattern, message: "Enter a valid last name" },
                ]}
              >
                <Input placeholder="Doe" />
              </Form.Item>
            </div>
            <div className="add-trader-field">
              <Form.Item
                label="Email"
                name="email"
                rules={[
                  { required: true, message: "Email is required" },
                  { type: "email", message: "Enter a valid email" },
                ]}
              >
                <Input placeholder="john.doe@example.com" />
              </Form.Item>
            </div>
            <div className="add-trader-field">
              <Form.Item
                label="Country"
                name="country"
                rules={[
                  { required: true, message: "Country is required" },
                  { pattern: countryPattern, message: "Enter a valid country" },
                ]}
              >
                <Input placeholder="CA" />
              </Form.Item>
            </div>
            <div className="add-trader-field">
              <Form.Item
                label="Date of Birth"
                name="dob"
                rules={[{ required: true, message: "DOB is required" }]}
              >
                <DatePicker style={{ width: "100%" }} />
              </Form.Item>
            </div>
          </div>
        </Form>
      </Modal>
    </div>
  );
}
